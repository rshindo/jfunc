実装対象と制約を確認したうえで進めます。まず `context/task` の主仕様、今回のレポート、実装ポリシー、既存コードとテストを突き合わせて、変更対象を確定します。
仕様と既存テストを確認し、今回必要なのは `Either#toResult()` と `Result#toEither()` の追加に絞れると判断しました。次にレポートを明示的に読み、スコープ宣言を作成してから実装に入ります。
レポートで変更対象と方針を確認しました。ここでスコープ宣言を先に残し、その後 `Try` と `README` の既存パターンを見て、同じ流儀で実装します。
これから実ファイルを編集します。変更は `Either` と `Result` の相互変換 API 追加、README の変換方針追記、そして実装開始時のスコープ宣言作成に限定します。
実装自体は入りました。ここから自己チェックとして、追加 API の宣言と呼び出し先が意図どおり一致しているかを確認し、その後にビルドと `mvn test` を順に実行します。
コンパイルを実行中です。Maven からの警告は依存由来で、この時点では失敗要因ではないため、完了まで待って結果を確定させます。
コンパイルは通りました。続けて `mvn test` を実行し、追加 API を含めて既存テスト一式が通ることを確認します。
テストは全件通過しました。最後に変更差分を確認し、余計な追加や取りこぼしがないことを見たうえで、実施結果をまとめます。
## 作業結果
- [`Either.java`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Either.java) に `toResult()` を追加し、`Left -> Result.failure`、`Right -> Result.success` を各 variant 側で実装しました。
- [`Result.java`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/main/java/com/github/rshindo/jfunc/Result.java) に `toEither()` を追加し、`Failure -> Either.left`、`Success -> Either.right` を各 variant 側で実装しました。
- 実装開始時のスコープ宣言を [`coder-scope.md`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/.takt/runs/20260414-042821-issue-2-option-either-result-t/reports/coder-scope.md) に作成しました。

## 変更内容
- [`README.md`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/README.md) に `Either.toResult()` / `Result.toEither()` を追記し、保持型変換と縮約型変換の方針を明記しました。
- 既存テスト [`EitherTest.java`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/EitherTest.java) と [`ResultTest.java`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/ResultTest.java) が期待する相互変換 API に合わせて、公開 API を最小追加で揃えました。
- default メソッドは使わず、既存方針どおり sealed interface の variant record に挙動を実装しています。

## ビルド結果
- `mvn -q -DskipTests compile`: 成功

## テスト結果
- `mvn test`: 成功
- 実行結果: 59 tests, 0 failures, 0 errors, 0 skipped