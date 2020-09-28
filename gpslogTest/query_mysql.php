<!DOCTYPE html>
<html lang="ja">
<head>
<!-- 
MySQLのテーブルを検索して結果を表示するページです。
 -->
<meta charset="UTF-8">
<title>sample table</title>
</head>
<body>
	<h1>sample table</h1>

<?php
const NL = "\n";
const CONSTR = 'mysql:host=localhost;dbname=gpslog;charset=utf8mb4';

echo '<table border="1">' . NL;
echo '<tr><th>doc id</th><th>name</th></tr>' . NL;

$pdo = new PDO(CONSTR, 'root', '');
$sql = 'select * from sample order by doc_id';
// $sql = $pdo->prepare ($sql);
// $sql->execute();
foreach ($pdo->query($sql) as $row) {
    echo '<tr>';
    echo '<td>', $row['doc_id'], '</td>';
    echo '<td>', $row['name'], '</td>';
    echo '</tr>' . NL;
}
echo '</table>' . NL;
?>

</body>
</html>
