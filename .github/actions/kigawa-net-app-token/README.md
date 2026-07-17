# kigawa-net-app-token

Reusable composite action that mints a short-lived installation token for the
`kigawa-net` GitHub App (app_id `4316503`, `contents:write` on all org repos),
so workflows can stop relying on the shared long-lived `GIT_TOKEN` PAT.

## One-time org setup (manual, do once)

1. On the App's settings page (org Settings → Developer settings → GitHub Apps →
   `kigawa-net`), click **Generate a private key** and download the `.pem` file.
2. Register it as an **organization secret** (Settings → Secrets and variables →
   Actions → Organization secrets), visible to the repos that need it:
   - Name: `KIGAWA_NET_APP_PRIVATE_KEY`
   - Value: contents of the downloaded `.pem` file

   Or via `gh` (must be run by an org admin, with the `.pem` file at hand):

   ```bash
   gh secret set KIGAWA_NET_APP_PRIVATE_KEY --org kigawa-net --visibility all < app-private-key.pem
   ```

The App ID (`4316503`) is not sensitive and is baked in as this action's default,
so no separate `APP_ID` secret is needed.

## Usage

```yaml
- name: Get GitHub App token
  id: app-token
  uses: kigawa-net/kinfra/.github/actions/kigawa-net-app-token@main
  with:
    private-key: ${{ secrets.KIGAWA_NET_APP_PRIVATE_KEY }}

- name: Checkout
  uses: actions/checkout@v4
  with:
    token: ${{ steps.app-token.outputs.token }}

# ... later, `git push` in this checkout authenticates as the App automatically.
```

To scope the token to specific repos (e.g. from a workflow in one repo that
needs to push to another), pass `repositories`:

```yaml
    repositories: kigawa-net-k8s
```
