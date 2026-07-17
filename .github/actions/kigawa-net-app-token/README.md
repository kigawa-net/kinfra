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
   `admin-panel-github-app` k8s Secret (`private-key` key) — see
   kigawa-net/admin-panel#46.
2. Pick a random shared secret and register it **twice**, with the same value:
   - As the `ci-token` key in the same `admin-panel-github-app` k8s Secret
     (admin-panel reads it as `GITHUB_APP_CI_TOKEN`).
   - As an **organization-level GitHub Actions secret** named
     `ADMIN_PANEL_CI_TOKEN`, visible to the repos that need it.

   Example for generating the value:

   ```bash
   openssl rand -hex 32
   ```

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
