# テスト作成レポート

## 作成テスト
| ファイル | 種別 | テスト数 | 概要 |
|---------|------|---------|------|
| `src/test/java/com/github/rshindo/jfunc/TryTest.java` | 単体 | 9 | `Try.recover` / `Try.recoverWith` の成功側 no-op、失敗側復旧、`null` mapper、`null` 戻り値を検証 |
| `src/test/java/com/github/rshindo/jfunc/ResultTest.java` | 単体 | 9 | `Result.recover` / `Result.recoverWith` の成功側 no-op、失敗側復旧、`null` mapper、`null` 戻り値を検証 |

## 実行結果（参考）
実装前のためテスト失敗・import エラーは想定内。

| 状態 | 件数 | 備考 |
|------|------|------|
| Pass | 0 | `mvn test` は `testCompile` で停止したため未実行 |
| Fail / Import Error（想定内） | 18 | `Try` / `Result` に `recover` / `recoverWith` が未実装のためコンパイルエラー |
| Error（要対応） | 0 | 既存パスミスや実装後も残る種類のエラーは未検出 |

## 備考（判断がある場合のみ）
- インテグレーションテストは未作成です。今回の変更対象は `Try` と `Result` 各単体の API 追加で、3モジュール以上を横断する新規データフローはありません。
- 成功側では mapper を評価しない no-op を `assertSame` と副作用未発生で固定しました。
- 失敗側の `recoverWith` は成功返却・失敗返却の両経路を分けて固定しました。