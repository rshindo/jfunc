# テスト作成レポート

## 作成テスト
| ファイル | 種別 | テスト数 | 概要 |
|---------|------|---------|------|
| `src/test/java/com/github/rshindo/jfunc/EitherTest.java` | 単体 | 2 | `Either#toResult()` の変換を追加予定 API として検証。`Right -> Result.success`、`Left -> Result.failure` を個別に確認 |
| `src/test/java/com/github/rshindo/jfunc/ResultTest.java` | 単体 | 2 | `Result#toEither()` の変換を追加予定 API として検証。`Success -> Either.right`、`Failure -> Either.left` を個別に確認 |

## 実行結果（参考）
実装前のためテスト失敗・import エラーは想定内。

| 状態 | 件数 | 備考 |
|------|------|------|
| Pass | 0 | `mvn test` は testCompile で停止 |
| Fail / Import Error（想定内） | 4 | 未実装 API `Either#toResult()` 2件、`Result#toEither()` 2件によるコンパイルエラー |
| Error（要対応） | 0 | 既存パスミスや実装後も残る別種エラーは確認されず |

## 備考（判断がある場合のみ）
- 既存テストパターンに合わせ、単体テストを既存の `EitherTest` と `ResultTest` に追記した。
- 統合テストは追加していない。今回の対象は `Either` と `Result` の相互変換 API であり、3モジュール以上を横断する新規フローには当たらないため。
- `mvn test` の失敗要因は追加予定 API の未実装のみで、次工程は `Either#toResult()` と `Result#toEither()` の実装に集中できる。