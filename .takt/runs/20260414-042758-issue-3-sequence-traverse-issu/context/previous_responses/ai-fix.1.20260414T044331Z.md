指摘の一次情報を `.takt/.../context/task` と Report Directory から読み直し、対象ファイルと問題箇所を事実確認します。まず仕様、レビュー結果、関連実装を開いて、該当箇所を検索で突き合わせます。
仕様とポリシーは確認しました。次に Report Directory のレポートを一次情報として開き、指摘対象のファイルと実際のコードを照合します。
Report Directory は特定できました。ここから `ai-review.md` を起点に、指摘対象の実装ファイルとテストファイルを直接開いて確認します。
AI レビューの指摘は 1 件で、`Option.traverse` の契約逸脱です。いまの実装では `Function<? super T, ?>` と実行時分岐が残っているので、該当行を行番号付きで確認したうえで契約を `Function<? super T, Option<R>>` に戻します。
これから `Option.java` と `OptionTest.java` を修正します。`traverse` のシグネチャと本体を `Option` 専用に絞り、テストの `String::length` のような生値 mapper を `Option.some(...)` を返す形へ合わせます。
実装は差し替えました。未検査キャストと `instanceof Option<?>` が消えていること、テスト側に生値 mapper が残っていないことを検索で再確認してから `mvn test` を実行します。
テストを走らせています。コンパイル段階までは進んでいるので、このまま完走結果を確認します。
## 確認したファイル
- `.takt/runs/20260414-042758-issue-3-sequence-traverse-issu/reports/ai-review.md`
- [`src/main/java/com/github/rshindo/jfunc/Option.java`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0428-3-issue-3-korekushon-toshite-seq/src/main/java/com/github/rshindo/jfunc/Option.java:89)
- [`src/test/java/com/github/rshindo/jfunc/OptionTest.java`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0428-3-issue-3-korekushon-toshite-seq/src/test/java/com/github/rshindo/jfunc/OptionTest.java:181)
- [`README.md`](/Users/shindo/git/rshindo/jfunc_worktrees/takt-worktrees/20260414T0428-3-issue-3-korekushon-toshite-seq/README.md:1)

## 実行した検索
- `find .takt/runs/20260414-042758-issue-3-sequence-traverse-issu -type f | grep -E 'report|review|ai_review|reviewers' | sort`
  - Report Directory 内の一次情報を特定し、`reports/ai-review.md` を確認しました。
- `grep -RIn "Function<? super T, ?>\\|instanceof Option\\|String::length\\|traverse(List" src/main/java src/test/java README.md`
  - `Option.java` の過剰に汎化された `traverse` と、`OptionTest.java` の生値 mapper 使用箇所を特定しました。
- `grep -RIn "Function<? super T, ?>\\|instanceof Option\\|String::length" src/main/java/com/github/rshindo/jfunc/Option.java src/test/java/com/github/rshindo/jfunc/OptionTest.java`
  - 修正後に再検索し、問題パターンが残っていないことを確認しました。該当なしのため終了コードは `1` でした。

## 修正内容
- `src/main/java/com/github/rshindo/jfunc/Option.java:89` を確認した結果、AI レビュー指摘どおり `traverse` が `Function<? super T, ?>` を受け、`instanceof Option<?>` と未検査キャストで生値 mapper と `Option` mapper を混在させていました。`Function<? super T, Option<R>>` に修正し、分岐も `switch (mapped)` のみへ簡素化しました。
- `src/test/java/com/github/rshindo/jfunc/OptionTest.java:181` を確認した結果、`String::length` をそのまま渡すテストが残っており、修正後の契約と不整合でした。空コレクション、全成功、`null` iterable、`null` 要素の各テストを `value -> Option.some(value.length())` に修正しました。
- `README.md` も確認しましたが、今回の AI レビュー指摘は `Option.traverse` の公開契約逸脱のみで、README にこの誤契約へ依存する記述はありませんでした。そのため変更していません。

## テスト結果
- 実行コマンド: `mvn test`
- 結果: `BUILD SUCCESS`
- サマリー: `Tests run: 83, Failures: 0, Errors: 0, Skipped: 0`