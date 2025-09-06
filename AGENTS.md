# 作業上の注意点 (jfunc)

このプロジェクトで作業する際のガイドラインと注意点をまとめます。コード変更時の一貫性・安全性・再現性の確保を目的としています。

## テスト運用
- 常に実行: コードを変更したら必ず `mvn test` を実行して結果を確認する。
- テスト場所: `src/test/java` 配下に JUnit 5 (Jupiter) でテストを追加する。既存は `OptionTest` を参照。
- 実行コマンド: `mvn test`（初回は依存取得あり）。ドキュメントのみの修正はテスト不要。

## ビルド/環境
- ビルド: Maven 使用。Jupiter は `test` スコープで追加済み、Surefire 3.2.x で実行。
- JDK: 現在の `pom.xml` は `maven.compiler.source/target = 24`。実行環境が 21 の場合は JDK を合わせるか、必要に応じて `21` に揃える（変更前に相談）。
- コマンド例:
  - テスト: `mvn test`
  - バージョン確認: `java -version`, `mvn -version`

## Option の設計方針
- 目的: Haskell の `Maybe` に相当。存在値は `Some`、欠損は `None`。
- 形態: `Option` は `sealed interface`。実装はネストされた `record Some` / `record None`。
- 実装先: 可能な限り default メソッドを避け、挙動は `Some`/`None` 側に実装する。
- 生成:
  - `Option.some(T)` は `null` を許容（`Some(null)`）。
  - `Option.none()` は毎回 `new None<>()`（シングルトンは現時点で採用しない）。
  - `Option.ofNullable(T)` は `null -> None`, 非 null -> `Some(value)`。
- 変換: `toOptional()` / `fromOptional(Optional)` を提供。
  - `Some(null).toOptional()` は `Optional.empty()`。
- 基本操作: `map` / `flatMap` / `filter` / `ifPresent`。
  - `map`: Some の値に適用し `some(...)` を返す。None は None。
  - `flatMap`: Some に適用し返却 `Option` をそのまま返す。None は None。
  - `filter`: 述語が偽なら None。None はそのまま。
  - `ifPresent`: Some のときのみ副作用を実行。
- 等価性: `record` による値ベースの `equals/hashCode`。`Option.none()` はインスタンスが異なっても等価比較は通る（同一性は保証しない）。

## 可視性/公開API
- 公開範囲は最小限に保つ。`Option` の公開 API を安易に広げない。
- 破壊的変更（メソッド削除/シグネチャ変更/意味変更）は事前に合意を取り、テストを更新する。

## コーディング規約
- 既存のスタイル・命名に合わせる。不要なリファクタやフォーマット変更は避ける。
- 変更は機能単位で最小限に。無関係な修正は別 PR/コミットに分離。
- Javadoc は日本語で要点を簡潔に。公開メソッド/型に付与する。
- 一文字変数や曖昧な命名を避け、読みやすさを優先。
 - パターンマッチ優先: `sealed` 型に対する API は、パターンマッチ（`switch`/型パターン）で表現可能な機能は極力追加しない。例: `Either.fold(...)` は提供しない。

## テスト追加の方針
- 新機能や仕様変更には、`Some` と `None` 双方の経路をカバーするテストを追加する。
- `null` 入力や端境ケース（空/境界値）もテストする。

## 禁止・控えること
- CI は導入しない（現時点の方針）。ローカルでのテスト実行を徹底する。
- `None` のシングルトン化は現状行わない（必要になれば再検討）。
- Java バージョンの変更、依存追加/更新は影響を確認し、事前に合意を取る。

## 参考ファイル
- 実装: `src/main/java/com/github/rshindo/jfunc/Option.java`
- テスト: `src/test/java/com/github/rshindo/jfunc/OptionTest.java`
- ビルド: `pom.xml`

## Either の設計方針
- 目的: 失敗/成功の直和型。右（`Right`）が成功、左（`Left`）が失敗/理由。
- 形態: `Either` は `sealed interface`。実装はネストされた `record Left` / `record Right`。`permits` 明示は省略。
- 実装先: default メソッドを避け、挙動は `Left`/`Right` 側に実装する。
- 生成:
  - `Either.right(R)` は `null` を非許容（例外）。
  - `Either.left(L)` は `null` を非許容（例外）。
- 基本操作（右優先の文脈）: `map` / `flatMap` / `ifRight`。
- 左側操作: `mapLeft` / `ifLeft`。
- 変換: `toOptionalRight()` / `toOptionalLeft()`。
- パターンマッチ優先: 左右の畳み込みは `switch` で行う。`fold` などパターンマッチで代替可能な API は追加しない。
