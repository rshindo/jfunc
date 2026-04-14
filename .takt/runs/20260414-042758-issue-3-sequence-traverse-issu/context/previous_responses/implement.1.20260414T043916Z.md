実装対象と制約を先に固めます。まず `.takt/.../context/task` の指示、今回の Report Directory 内のレポート、ポリシー全文、既存の実装と追加済みテストを読み、変更範囲を確定します。
Report Directory の既存レポート名を確認し、今回参照すべき一次情報を絞ります。そのうえで、実装開始時に必要な `coder-scope.md` をこのディレクトリへ作成します。
既存レポートの実体を確認したところ、`coder-scope.md` と `coder-decisions.md` はまだ未作成です。これからスコープ宣言を作成し、その後に本体実装へ入ります。
計画では README の使用例追加もスコープに入っています。まず実装の核になる `Option` / `Result` を入れ、その後 README に最小のサンプルだけ追記します。
これから `Option.java` と `Result.java` に static API を追加し、あわせて README に最小の利用例を追記します。テスト側は既に仕様を固定しているので、今回はプロダクションコードとドキュメントだけを編集します。
パターンマッチの書き方を既存テストに揃えます。Java 21 の record pattern では型引数を明示しないほうがこのコードベースと一貫しており、安全です。
実装は入ったので、自己チェックとして差分を確認し、不要なフォールバックや余計な API 追加がないかを先に見ます。その後 `mvn test` でビルド兼テストを通します。
`mvn test` を流しています。コンパイル段階の実エラーを見て、必要ならその場で実装を詰めます。
`Option.traverse` のテストが想定しているシグネチャは、`String::length` のような「生値 mapper」と `Option.none()` を返す mapper の両方を受ける形です。テストの行を確認して、仕様どおりに overload を揃えます。
`Option.traverse` はテストに従うと「生値を返す mapper」と「Option を返す mapper」の両方を受ける必要があります。Java の型消去で overload は作れないので、1 つのシグネチャで両方を受け、戻り値を実行時に判定する形へ揃えます。
コンパイルエラーは `Option.traverse` の契約差分だけでした。そこをテストどおりに吸収したので、再度 `mvn test` を最後まで流しています。
ビルドは通っていますが、`Option.traverse` の実行時分岐に由来する未検査キャスト警告だけ残っています。警告を局所化するため `@SuppressWarnings("unchecked")` をそのメソッドへ限定し、判断理由もレポートに残します。
最終要件に合わせて、テストとは別にビルド確認も明示的に通します。`mvn compile` を先に実行し、その後の `mvn test` 成功と分けて記録します。
## 作業結果
- `Option` と `Result` に `sequence` / `traverse` を実装し、追加済みテストが通る状態にしました。
- 実装開始時のスコープ宣言を [coder-scope.md](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0428-3-issue-3-korekushon-toshite-seq/.takt/runs/20260414-042758-issue-3-sequence-traverse-issu/reports/coder-scope.md) に作成し、`Option.traverse` の契約判断を [coder-decisions.md](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0428-3-issue-3-korekushon-toshite-seq/.takt/runs/20260414-042758-issue-3-sequence-traverse-issu/reports/coder-decisions.md) に記録しました。

## 変更内容
- [Option.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0428-3-issue-3-korekushon-toshite-seq/src/main/java/com/github/rshindo/jfunc/Option.java) に `sequence` / `traverse` を追加しました。`null` iterable・`null` 要素・`null` mapper・`null` 戻り値は `IllegalArgumentException`、`None` で短絡、全成功時は `List` を返します。
- [Result.java](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0428-3-issue-3-korekushon-toshite-seq/src/main/java/com/github/rshindo/jfunc/Result.java) に `sequence` / `traverse` を追加しました。最初の `Failure` で短絡し、全成功時は `Success<List<...>>` を返します。
- [README.md](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0428-3-issue-3-korekushon-toshite-seq/README.md) に `Option.sequence` と `Result.traverse` の使用例を追記しました。

## ビルド結果
- `mvn compile`: 成功

## テスト結果
- `mvn test`: 成功
- 実行結果: 83 tests, 0 failures, 0 errors, 0 skipped