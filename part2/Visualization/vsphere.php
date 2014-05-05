<?php 

function getVmsByType ($connection, $vmType)
{
    $sql = "SELECT distinct(machineName) FROM cpu WHERE machineType = '$vmType' Order By machineName";
    $sqlResult = mysqli_query($connection, $sql);
	if (!$sqlResult) {
		die("Database query failed.....");
	}

	$result = array();
    while ($row = mysqli_fetch_array($sqlResult)) {
        $result[] = $row;
    }

    return $result;
}

?>