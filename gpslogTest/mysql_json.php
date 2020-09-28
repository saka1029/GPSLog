<?php
/*******************
 * データベースを検索して結果をjsonで返すサンプルです。
 *******************/
const CONSTR = 'mysql:host=localhost;dbname=gpslog;charset=utf8mb4';

header("Content-Type: application/json; charset=utf-8");
#header("Content-Type: text/javascript; charset=utf-8");
#header("Content-Type: application/javascript; charset=utf-8"); # 最近はこっちが正しいらしい

$k = '%' . $_GET['k'] . '%';  # query string 'k' を取り出す

$pdo = new PDO (CONSTR, 'root', '');
$sql = 'select * from sample where name like :k order by doc_id limit 20';
$prepare = $pdo->prepare($sql);
$prepare->bindValue(':k', $k, PDO::PARAM_STR);
$prepare->execute();
$res = $prepare->fetchAll(PDO::FETCH_ASSOC);

echo '[';
$sep = '';
foreach ($res as $r) {
#   error_log(print_r($r, true));
    echo $sep;
    echo json_encode(array(
        'doc_id' => $r['doc_id'],
        'name' => $r['name']));
    $sep = ',';
}

echo ']';
?>
