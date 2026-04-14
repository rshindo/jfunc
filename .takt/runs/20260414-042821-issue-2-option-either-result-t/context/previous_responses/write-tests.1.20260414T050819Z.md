このステップではテストだけを追加します。まず指定された `context/task` の主仕様と、このステップ用のポリシー／レポートを確認して、追加するテストケースを既存実装と照合します。
主仕様とポリシーは確認できました。レポートは `plan.md` だけなので、そこを読み、計画済みの変換 API と期待動作をテストに落とします。
既存のテスト様式は把握できました。次に `Either` / `Result` / `Try` のテストを確認して、重複なく不足分だけ追加します。
追加するのは `Either#toResult()` と `Result#toEither()` を前提にした単体テストです。既存の命名に合わせて、成功側・失敗側を分けた4件を既存テストファイルへ追記します。
テスト追加は完了しました。ここから `mvn test` を実行して、未実装 API による想定どおりの失敗か、テスト側の記述ミスがないかを切り分けます。
コンパイル段階に入っています。未実装 API に起因する失敗ならそのまま次工程へ渡せるので、エラー箇所を確定させます。
失敗は想定どおりで、未実装の公開 API だけが欠けています。`Either#toResult()` と `Result#toEither()` のコンパイルエラー以外は出ていないので、次工程はその実装に集中できます。
[`EitherTest`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/EitherTest:83) に `Either#toResult()` 前提の2件を追加し、`Right -> Success` と `Left -> Failure` を分けて検証する形にしました。[`ResultTest`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0503-2-issue-2-option-either-result-t/src/test/java/com/github/rshindo/jfunc/ResultTest:114) には `Result#toEither()` 前提の2件を追加し、`Success -> Right` と `Failure -> Left` を確認しています。

インテグレーションテストは追加していません。今回の変更対象は `Either` と `Result` の相互変換 API で、3モジュール以上を横断する新しいデータフローではないため、この段階では単体テストで十分です。

`mvn test` は実行済みで、想定どおり未実装 API によるコンパイルエラーで失敗しています。失敗箇所は `EitherTest` の `toResult()` 呼び出しと `ResultTest` の `toEither()` 呼び出しのみで、次工程で `Either#toResult()` と `Result#toEither()` を実装すれば進められます。