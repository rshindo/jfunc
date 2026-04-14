# 決定ログ

## 1. `Option.traverse` は単一シグネチャで生値 mapper と `Option` mapper の両方を受ける
- **背景**: 追加済みテストが `String::length` のような生値 mapper と `Option.none()` / `Option.some(...)` を返す mapper の両方を `Option.traverse` に渡していた
- **検討した選択肢**: `Function<? super T, Option<R>>` のみ受ける; 生値用と `Option` 用で overload を分ける; `Function<? super T, ?>` で単一シグネチャに集約する
- **理由**: Java の型消去で `Function` の戻り値違い overload は作れず、テスト契約を満たすには単一シグネチャで戻り値を判定する実装が最小変更だった