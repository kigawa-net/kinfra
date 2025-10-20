# YAML構文エラー修正 - 2025年10月20日

## 問題

GitHub Actions実行 #18632509092 でYAML構文エラーが発生

**エラーメッセージ**:
```
Invalid workflow file
You have an error in your yaml syntax on line 108
```

**原因**: `.github/workflows/create-release-branch.yml` の108行目でインデントが不正

## 修正内容

### ファイル: `.github/workflows/create-release-branch.yml`

**修正前**:
```yaml
esac

VERSION="${MAJOR}.${MINOR}.${PATCH}"
          fi
```

**修正後**:
```yaml
esac

            VERSION="${MAJOR}.${MINOR}.${PATCH}"
          fi
```

**変更点**:
- 108行目の `VERSION="${MAJOR}.${MINOR}.${PATCH}"` のインデントを修正
- `esac` の後なので、`else` ブロックと同じインデントレベル（12スペース）に調整

## 検証

- ✅ 修正をコミット (f2f0569)
- ✅ プッシュ完了 (kigawaブランチ)
- 🔄 GitHub Actionsの再実行を監視中

## 技術的詳細

YAMLではインデントが構文の重要な要素です。`case`文の`esac`の後にあるコードは、`else`ブロックの一部として正しいインデントが必要でした。元のコードではインデントが不足しており、YAMLパーサーが構文エラーを検出していました。

## 次のステップ

1. GitHub Actionsが正常に実行されることを確認
2. リリースブランチ作成ワークフローが機能することを検証
3. 必要に応じてdevブランチにマージ