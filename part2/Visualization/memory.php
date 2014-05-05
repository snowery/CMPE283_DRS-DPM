<?php 

/**
 * [getAveragePrices : Grabs data from db]
 */

function getMemorysForOneVm ($connection, $vmName, $QueryNumber)
{
    $sqlAverageQuery = "SELECT timestamp, percent FROM mem WHERE machineName = '$vmName' Order By timestamp DESC limit {$QueryNumber} ";
    $sqlAverageResult = mysqli_query($connection, $sqlAverageQuery);
	if (!$sqlAverageResult) {
		die("Database query failed.....");
	}
	//else echo "query success";

    while ($row = mysqli_fetch_array($sqlAverageResult)) {
        $averageResult[] = $row;
    }

    return $averageResult;
}


function buildMemoryRatesArray($data_array)
{

    $output = "['Time', '% Memory'], ";
	$i=0;
    // The data needs to be in a format ['string', decimal, int]
   while (!empty($data_array[$i]) ){
        $output .= "['" . $data_array[$i]['timestamp'] . "', ";
        $output .= $data_array[$i]['percent'] . ", ";
        // On the final count do not add a comma
        if (!empty($data_array[$i+1]) ){
            $output .= "],\n";
        } else {
            $output .= "]\n";
        }
		$i++;
    };

    return $output;
}