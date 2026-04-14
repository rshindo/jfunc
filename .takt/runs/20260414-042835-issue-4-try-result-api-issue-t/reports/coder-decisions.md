# 決定ログ

## 1. `recover` / `recoverWith` の inactive 側は no-op とし、元インスタンスを返す
- **背景**: 追加済みテストでは `Success` 側の `recover` / `recoverWith` が mapper を評価せず、同一インスタンスを返すことが期待されていたため。
- **検討した選択肢**: `Success` 側でも mapper の null チェックだけ行う; `Success` 側は完全 no-op で `this` を返す。
- **理由**: 右バイアス設計を維持しつつ、既存 `map` / `flatMap` の inactive-side no-op パターンと整合するため。

## 2. active 側で `recoverWith` mapper が `null` を返した場合は `IllegalArgumentException` にする
- **背景**: 受け入れ条件と追加済みテストで、mapper 自体が `null` の場合と mapper 戻り値が `null` の場合の挙動を明確に分ける必要があったため。
- **検討した選択肢**: `null` をそのまま返す; `NullPointerException` にする; `IllegalArgumentException` にする。
- **理由**: このライブラリの null 非許容方針に合わせ、`recover` の `null` 戻り値が factory 経由で `IllegalArgumentException` になる挙動と揃えられるため。

## 3. README には最小限の復旧例だけを追加する
- **背景**: 受け入れ条件に README の典型例追加が含まれていた一方、今回のタスクは最小 API の実装に限定されていたため。
- **検討した選択肢**: README を変更しない; 詳細な解説節を新設する; 既存の `Result` / `Try` サンプル直下に短い例を追記する。
- **理由**: 既存構成を崩さず、タスク指示書にない大きな構造変更を避けながら利用例を満たせるため。