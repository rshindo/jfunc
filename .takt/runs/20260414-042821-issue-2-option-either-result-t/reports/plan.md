# タスク計画

## 元の要求
Implement using only the files in `.takt/runs/20260414-042821-issue-2-option-either-result-t/context/task`.
Primary spec: `.takt/runs/20260414-042821-issue-2-option-either-result-t/context/task/order.md`.
Use report files in Report Directory as primary execution history.
Do not rely on previous response or conversation summary.

## 分析結果

### 目的
`Option` / `Either` / `Result` / `Try` 間の変換 API を一貫化し、今回未整備な `Either <-> Result` を追加しつつ、既存の `Try -> Either` / `Try -> Result` / 各型から `Option` への変換を維持し、変換方針を README または Javadoc で明示する。

### 分解した要件
| # | 要件 | 種別 | 備考 |
|---|------|------|------|
| 1 | `Either` から `Result` へ変換できること | 明示 | issue の優先実装候補 |
| 2 | `Result` から `Either` へ変換できること | 明示 | issue の優先実装候補 |
| 3 | `Try` から `Either` への変換が提供されていること | 明示 | 現状確認対象 |
| 4 | `Try` から `Result` への変換が提供されていること | 明示 | 現状確認対象 |
| 5 | `Either` から `Option` への変換が提供されていること | 明示 | 右側/左側を分けて確認 |
| 6 | `Result` から `Option` への変換が提供されていること | 明示 | 成功側/失敗側を分けて確認 |
| 7 | `Try` から `Option` への変換が提供されていること | 明示 | 成功側/失敗側を分けて確認 |
| 8 | 既存公開 API の意味変更を避けること | 明示 | 新規追加のみで対応する |
| 9 | `fold` のような汎用ヘルパーを追加しないこと | 明示 | パターンマッチ優先を維持 |
| 10 | 変換方針を README または Javadoc で明示すること | 明示 | 受け入れ条件 |
| 11 | `Left` / `Right` / `Success` / `Failure` の各経路をテストすること | 明示 | 追加 API の両経路を網羅 |
| 12 | `null` ポリシーが既存設計と矛盾しないこと | 明示 | 既存 factory / record 制約を維持 |
| 13 | 新規変換 API も default メソッドではなく variant 側に実装すること | 暗黙 | AGENTS.md の `Either` / `Option` 設計方針から直接導出 |
| 14 | 利用者が新 API に直接到達できるよう public API に最小追加すること | 暗黙 | 明示要求の「型間変換 API 提供」から直接導出 |

### 参照資料の調査結果（参照資料がある場合）
参照資料は `.takt/runs/20260414-042821-issue-2-option-either-result-t/context/task/order.md` のみだった。内容は、変換 API を「保持型変換」と「縮約型変換」に整理し、少なくとも `Either <-> Result`、`Try -> Either`、`Try -> Result`、`Either` / `Result` / `Try` -> `Option` を検討対象とするものだった。

現行実装との差異は以下の通り。
- `Try -> Either` は既に `Try.toEither()` として実装済み。
- `Try -> Result` は既に `Try.toResult()` として実装済み。
- `Either -> Option`、`Result -> Option`、`Try -> Option` も既に実装済み。
- 未実装なのは `Either -> Result` と `Result -> Either`。
- README は型ごとの個別説明はあるが、変換方針の一覧化や変換マトリクスは未整備。

### スコープ
- 変更対象
  - `src/main/java/com/github/rshindo/jfunc/Either.java`
  - `src/main/java/com/github/rshindo/jfunc/Result.java`
  - `src/test/java/com/github/rshindo/jfunc/EitherTest.java`
  - `src/test/java/com/github/rshindo/jfunc/ResultTest.java`
  - `README.md`
- 参照のみ
  - `src/main/java/com/github/rshindo/jfunc/Try.java`
  - `src/test/java/com/github/rshindo/jfunc/TryTest.java`
- 影響内容
  - 公開 API に `Either#toResult()` と `Result#toEither()` を追加
  - 対応する両経路テストを追加
  - 変換方針の文書化を追加
- 配線変更
  - なし。ライブラリ利用者が各インスタンスメソッドを直接呼ぶ形のため、別レイヤーや設定配線は存在しない

### 検討したアプローチ（設計判断がある場合）
| アプローチ | 採否 | 理由 |
|-----------|------|------|
| `Either` と `Result` に相互変換メソッドを直接追加する | 採用 | 最小 API で要件を満たし、既存の `Try.toEither()` / `Try.toResult()` と命名粒度が揃う |
| 変換専用ユーティリティクラスを新設する | 不採用 | API が分散し、最小 API 方針と操作一覧性を崩す |
| `fold` や汎用変換ヘルパーを追加する | 不採用 | 参照資料と AGENTS.md のパターンマッチ優先方針に反する |
| `Either` / `Result` の片側だけ追加して相互変換は利用者に任せる | 不採用 | issue の優先候補である相互変換の一貫化を満たさない |
| Javadoc のみで方針を明示する | 条件付き不採用 | 可能ではあるが、README の方が変換マトリクスを一覧化しやすい |
| README に変換方針を追加する | 採用 | 受け入れ条件を明確に満たせて、型横断の整理に向いている |

### 実装アプローチ
`Either` と `Result` の sealed interface に抽象変換メソッドを追加し、実処理は各 record variant に実装する。

- `Either.Left` は `Result.failure(value)` を返す
- `Either.Right` は `Result.success(value)` を返す
- `Result.Success` は `Either.right(value)` を返す
- `Result.Failure` は `Either.left(error)` を返す

既存パターンに合わせ、変換時に余計な分岐ヘルパーや default メソッドは導入しない。`null` ポリシーは既存 factory / record 検証に委ね、新しい `null` 許容経路は作らない。

テストは `EitherTest` と `ResultTest` に追加し、各変換で両経路を確認する。`TryTest` は既存カバレッジがあるため原則変更不要。README には変換 API の一覧または簡易マトリクスを追記し、「保持型変換」と「縮約型変換」の区別が分かるようにする。

### 到達経路・起動条件（利用者向け機能の追加/変更がある場合）
| 項目 | 内容 |
|------|------|
| 利用者が到達する入口 | `Either` / `Result` の公開インスタンスメソッド |
| 更新が必要な呼び出し元・配線 | なし。型自身の API 追加のみ |
| 起動条件 | 対象型のインスタンスを保持していること |
| 未対応項目 | なし |

## 実装ガイドライン（設計が必要な場合のみ）
- `Either` / `Result` への新規 API 追加は abstract メソッド宣言に留め、挙動は各 variant record に実装する。default メソッドは使わない。
- 参照すべき既存実装パターンは `Either.toOptionRight()` / `toOptionLeft()`、`Try.toEither()`、`Try.toResult()`。
- `EitherTest` と `ResultTest` では追加 API ごとに正経路・逆経路を1つずつではなく、variant ごとの意味を明確にするテスト名で両経路を分けて追加する。
- README では型ごとの Features 列挙に埋め込むだけでなく、変換の全体像が一目で分かる形を優先する。
- 公開 API の意味変更、既存メソッド名の変更、`fold` 系 API 追加、変換ユーティリティ新設は行わない。
- `Try.java` は既に 300 行超だが、今回不足している API ではないためリファクタリング対象に含めない。
- コード変更後は `mvn test` 実行が必須。

## スコープ外（項目がある場合のみ）
| 項目 | 除外理由 |
|------|---------|
| `Try` の追加 API | `toEither()` / `toResult()` が既に存在し、要件を満たしているため |
| `Option` の追加 API | 今回の要求は型間変換の一貫化であり、`Option` 側の新規入口追加は不要なため |
| `fold` / 汎用ヘルパー導入 | 明示的に禁止されているため |
| `Try.java` の分割リファクタリング | 既存構造課題はあるが今回の要求から直接導かれず、スコープ外のため |

## 確認事項（あれば）
- なし