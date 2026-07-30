# kigawa-net-app-token

Reusable composite action that mints a short-lived installation token for the
`kigawa-net` GitHub App (app_id `4316503`, `contents:write` on all org repos),
so workflows can stop relying on the shared long-lived `GIT_TOKEN` PAT.

**Flow: custom action (this) -> admin-panel (verifies OIDC) -> GitHub App.**
The App's private key lives only on the admin-panel server
(kigawa-net/admin-panel#41, kigawa-net/admin-panel#46). This action proves
its identity to admin-panel's broker endpoint (`POST /api/github-app/ci-token`)
with its own GitHub Actions OIDC token — no shared secret is involved on
either side. admin-panel verifies the token against GitHub's own published
keys and authorizes the request based on the token's `repository` claim (see
`CiTokenPolicy.kt` in admin-panel), not on anything the caller can spoof.

## One-time org setup (manual, do once)

1. On admin-panel's side, an org admin generates the App's private key
   (App settings → Generate a private key) and registers it as the
   `admin-panel-github-app` k8s Secret (`private-key` key), via the
   `BitwardenSecret` CRD in `k8s/base/github-app-bws.yaml` — see
   kigawa-net/admin-panel#46.
2. Add an entry for the calling repository to admin-panel's
   `CiTokenPolicy.kt` (allowed target owner/repositories/permissions) and
   merge that PR — this is the only "registration" a new caller needs.

No shared secret needs to be generated, stored, or rotated for CI's use of
this action.

## Usage

The calling job must request an OIDC token:

```yaml
permissions:
  id-token: write

steps:
  - name: Get GitHub App token
    id: app-token
    uses: kigawa-net/kinfra/.github/actions/kigawa-net-app-token@main
    with:
      owner: kigawa-net
      repositories: kigawa-net-k8s
      permissions: '{"contents":"write"}'

  - name: Checkout
    uses: actions/checkout@v4
    with:
      token: ${{ steps.app-token.outputs.token }}

  # ... later, `git push` in this checkout authenticates as the App automatically.
```
