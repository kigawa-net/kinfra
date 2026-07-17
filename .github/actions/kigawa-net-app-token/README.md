# kigawa-net-app-token

Reusable composite action that mints a short-lived installation token for the
`kigawa-net` GitHub App (app_id `4316503`, `contents:write` on all org repos),
so workflows can stop relying on the shared long-lived `GIT_TOKEN` PAT.

**Flow: custom action (this) -> admin-panel -> GitHub App.** The App's
private key lives only on the admin-panel server (kigawa-net/admin-panel#41,
kigawa-net/admin-panel#46); this action never sees it. It authenticates to
admin-panel's broker endpoint (`POST /api/github-app/ci-token`) with a shared
CI token instead.

## One-time org setup (manual, do once)

1. On admin-panel's side, an org admin generates the App's private key
   (App settings → Generate a private key) and registers it as the
   `admin-panel-github-app` k8s Secret (`private-key` key), via the
   `BitwardenSecret` CRD in `k8s/base/github-app-bws.yaml` — see
   kigawa-net/admin-panel#46.
2. The shared CI token is generated and wired up entirely by Terraform
   (`platform/admin-panel` in kigawa-net/infra#35): it's stored in Bitwarden
   as the `ci-token` key of the same `admin-panel-github-app` k8s Secret,
   *and* registered directly as the organization-level GitHub Actions
   secret `ADMIN_PANEL_CI_TOKEN` (visible only to admin-panel and kinfra) —
   no manual `gh secret set` needed. The only remaining manual step is
   minting the `admin:org`-scoped GitHub PAT that module's `github` provider
   authenticates with; see kigawa-net/infra#35 for details.

## Usage

```yaml
- name: Get GitHub App token
  id: app-token
  uses: kigawa-net/kinfra/.github/actions/kigawa-net-app-token@main
  with:
    ci-token: ${{ secrets.ADMIN_PANEL_CI_TOKEN }}

- name: Checkout
  uses: actions/checkout@v4
  with:
    token: ${{ steps.app-token.outputs.token }}

# ... later, `git push` in this checkout authenticates as the App automatically.
```

To scope the token to specific repos or a permission subset:

```yaml
    repositories: kigawa-net-k8s
    permissions: '{"contents":"write"}'
```
