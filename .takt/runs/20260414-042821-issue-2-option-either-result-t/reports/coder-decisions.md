# 決定ログ

## 1. `Either` / `Result` の相互変換は各 variant record に直接実装する
- **背景**: `Either#toResult()` と `Result#toEither()` を追加する際、既存の sealed interface / nested record 構成と default メソッド回避方針に合わせる必要があった
- **検討した選択肢**: sealed interface に default 実装を置く, 変換専用ユーティリティを追加する, `Left` / `Right` / `Success` / `Failure` の各 variant に実装する
- **理由**: 既存の `toOption*()` や `Try` の変換 API と同じ実装パターンを維持でき、公開 API を最小限に保ったまま挙動を各 variant に閉じ込められるため

## 2. 変換方針の明示は README で行う
- **背景**: 受け入れ条件に「型間変換の提供方針が README または Javadoc で明示されていること」があり、追加 API を型横断で一覧化する必要があった
- **検討した選択肢**: `Either` と `Result` の Javadoc のみ更新する, README に変換方針を追記する
- **理由**: 保持型変換と縮約型変換を横断的に整理でき、利用者が変換 API 全体像を一箇所で確認しやすいため