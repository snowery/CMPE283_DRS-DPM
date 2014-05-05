<?php require_once('db_connection.php'); ?>
<?php require_once('config.php'); ?>
<?php include("vsphere.php"); ?>
<?php include("cpu.php"); ?>
<?php include("memory.php"); ?>
<?php include("disk.php"); ?>

<head>

    <meta charset="utf-8">
    <!-- <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"> -->

    <title>CMPE283Lab3</title>

    <meta name="viewport" content="width=device-width">

    <style type="text/css">
        #chart {
            height: 400px;
            width: 100%;
        }

        #siteWrapper {
            padding: 2em;
        }

        h1 {
            font: bold 2em/1.5 sans-serif;
        }
    </style>

</head>

<body>

<div id="siteWrapper">

    <div class="container">
        <h1>CPU Usage of VHosts </h1>
        <table>
            <tr>
                <?php
                $vmNames = getVmsByType($connection, "HostSystem");

                for ($i = 0; $i < count($vmNames); $i++) {
                    ?>
                    <td height=200px width=500px>
                        <?php echo $vmNames[$i]["machineName"] ?>
                        <div id="CPUchart_HostSystem<?php echo $i ?>"></div>
                    </td>
                <?php } ?>
            <tr>
        </table>

        <h1>CPU Usage of Virtual Machines </h1>
        <table>
            <tr>
                <?php
                $vmNames = getVmsByType($connection, "VirtualMachine");

                for ($i = 0; $i < count($vmNames); $i++) {
                    ?>
                    <td height=200px width=500px>
                        <?php echo $vmNames[$i]["machineName"] ?>
                        <div id="CPUchart_VirtualMachine<?php echo $i ?>"></div>
                    </td>
                <?php } ?>
            <tr>
        </table>

        <h1>Memory Usage of VHosts </h1>
        <table>
            <tr>
                <?php
                $vmNames = getVmsByType($connection, "HostSystem");

                for ($i = 0; $i < count($vmNames); $i++) {
                    ?>
                    <td height=200px width=500px>
                        <?php echo $vmNames[$i]["machineName"] ?>
                        <div id="Memorychart_HostSystem<?php echo $i ?>"></div>
                    </td>
                <?php } ?>
            <tr>
        </table>

        <h1>Memory Usage of Virtual Machines </h1>
        <table>
            <tr>
                <?php
                $vmNames = getVmsByType($connection, "VirtualMachine");

                for ($i = 0; $i < count($vmNames); $i++) {
                    ?>
                    <td height=200px width=500px>
                        <?php echo $vmNames[$i]["machineName"] ?>
                        <div id="Memorychart_VirtualMachine<?php echo $i ?>"></div>
                    </td>
                <?php } ?>
            <tr>
        </table>

        <h1>I/O Usage of VHosts </h1>
        <table>
            <tr>
                <?php
                $vmNames = getVmsByType($connection, "HostSystem");

                for ($i = 0; $i < count($vmNames); $i++) {
                    ?>
                    <td height=200px width=500px>
                        <?php echo $vmNames[$i]["machineName"] ?>
                        <div id="IORWchart_HostSystem<?php echo $i ?>"></div>
                    </td>
                <?php } ?>
            <tr>
        </table>

        <h1>I/O Usage of Virtual Machines </h1>
        <table>
            <tr>
                <?php
                $vmNames = getVmsByType($connection, "VirtualMachine");

                for ($i = 0; $i < count($vmNames); $i++) {
                    ?>
                    <td height=200px width=500px>
                        <?php echo $vmNames[$i]["machineName"] ?>
                        <div id="IORWchart_VirtualMachine<?php echo $i ?>"></div>
                    </td>
                <?php } ?>
            <tr>
        </table>

    </div>

</div>


<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">
    google.load("visualization", "1", {packages: ["corechart"]});
    google.setOnLoadCallback(drawAllChart);

    function drawAllChart() {

        drawCPUChart();
        drawMemoryChart();
        drawIOChart();
        //setTimeout("drawAllChart()", 6000);
    }

    function drawCPUChart() {

        var cpuoptions = {
            title: 'CPU Usage',
            fontSize: 11,
            curveType: 'function',
            series: {
                0: {color: 'red', visibleInLegend: true, pointSize: 3, lineWidth: 1}
            },
            hAxis: {title: 'Time', titleTextStyle: {color: '#03619D'}},
            vAxis: {title: 'CPU Usage', titleTextStyle: {color: '#03619D'}}
        };

        <?php

        $vmNames = getVmsByType($connection, "HostSystem");

        for ($i = 0; $i < count($vmNames); $i++) {
            $data_array = getCPUsForOneVm($connection, $vmNames[$i]['machineName'],$QueryNumber);
            $graphCPUData = buildCPUsArray($data_array);
        ?>
        var cpudata<?php echo $i ?> = google.visualization.arrayToDataTable([
            <?php echo $graphCPUData ?>
        ]);

        var cpuchart<?php echo $i ?> = new google.visualization.ColumnChart(document.getElementById('CPUchart_HostSystem<?php echo $i ?>'));
        cpuchart<?php echo $i ?>.draw(cpudata<?php echo $i ?>, cpuoptions);
        <?php };

        $vmNames = getVmsByType($connection, "VirtualMachine");

        for ($i = 0; $i < count($vmNames); $i++) {
            $data_array = getCPUsForOneVm($connection, $vmNames[$i]['machineName'],$QueryNumber);
            $graphCPUData = buildCPUsArray($data_array);
            ?>
        var cpudata<?php echo $i ?> = google.visualization.arrayToDataTable([
            <?php echo $graphCPUData ?>
        ]);

        var cpuchart<?php echo $i ?> = new google.visualization.ColumnChart(document.getElementById('CPUchart_VirtualMachine<?php echo $i ?>'));
        cpuchart<?php echo $i ?>.draw(cpudata<?php echo $i ?>, cpuoptions);
        <?php }; ?>

    }

    function drawMemoryChart() {

        var options = {
            title: 'Memory Usage' ,
            fontSize: 11,
            series: {
                0:{color: 'green', visibleInLegend: true, pointSize: 5, lineWidth: 3}
            },
            hAxis: {title: 'Time', titleTextStyle:{color: '#03619D'}},
            vAxis: {title: 'Memory Usage', titleTextStyle:{color: '#03619D'}}
        };

        <?php

        $vmNames = getVmsByType($connection, "HostSystem");

        for ($i = 0; $i < count($vmNames); $i++) {
            $data_array = getMemorysForOneVm($connection, $vmNames[$i]['machineName'], $QueryNumber);
            $graphData = buildMemoryRatesArray($data_array);
        ?>
        var cpudata<?php echo $i ?> = google.visualization.arrayToDataTable([
            <?php echo $graphData ?>
        ]);

        var cpuchart<?php echo $i ?> = new google.visualization.ColumnChart(document.getElementById('Memorychart_HostSystem<?php echo $i ?>'));
        cpuchart<?php echo $i ?>.draw(cpudata<?php echo $i ?>, options);
        <?php };

        $vmNames = getVmsByType($connection, "VirtualMachine");

        for ($i = 0; $i < count($vmNames); $i++) {
            $data_array = getMemorysForOneVm($connection, $vmNames[$i]['machineName'], $QueryNumber);
            $graphData = buildMemoryRatesArray($data_array);
            ?>
        var cpudata<?php echo $i ?> = google.visualization.arrayToDataTable([
            <?php echo $graphData ?>
        ]);

        var cpuchart<?php echo $i ?> = new google.visualization.ColumnChart(document.getElementById('Memorychart_VirtualMachine<?php echo $i ?>'));
        cpuchart<?php echo $i ?>.draw(cpudata<?php echo $i ?>, options);
        <?php }; ?>

    }

    function drawIOChart() {

        var options = {
            title: 'Disk Read / Write' ,
            fontSize: 11,
            isStacked: true,
            series: {
                0:{color: 'green', visibleInLegend: true, pointSize: 3, lineWidth: 1},
                1:{color: 'blue', visibleInLegend: true, pointSize: 5, lineWidth: 3}
            },
            hAxis: {title: 'Time', titleTextStyle:{color: '#03619D'}},
            vAxis: {title: 'I/O Usage', titleTextStyle:{color: '#03619D'}}
        };

        <?php

        $vmNames = getVmsByType($connection, "HostSystem");

        for ($i = 0; $i < count($vmNames); $i++) {
            $data_array = getIOsForOneVm($connection, $vmNames[$i]['machineName'], $QueryNumber);
            $graphData = buildIORWsArray($data_array);
        ?>
        var cpudata<?php echo $i ?> = google.visualization.arrayToDataTable([
            <?php echo $graphData ?>
        ]);

        var cpuchart<?php echo $i ?> = new google.visualization.ColumnChart(document.getElementById('IORWchart_HostSystem<?php echo $i ?>'));
        cpuchart<?php echo $i ?>.draw(cpudata<?php echo $i ?>, options);
        <?php };

        $vmNames = getVmsByType($connection, "VirtualMachine");

        for ($i = 0; $i < count($vmNames); $i++) {
            $data_array = getIOsForOneVm($connection, $vmNames[$i]['machineName'], $QueryNumber);
            $graphData = buildIORWsArray($data_array);
            ?>
        var cpudata<?php echo $i ?> = google.visualization.arrayToDataTable([
            <?php echo $graphData ?>
        ]);

        var cpuchart<?php echo $i ?> = new google.visualization.ColumnChart(document.getElementById('IORWchart_VirtualMachine<?php echo $i ?>'));
        cpuchart<?php echo $i ?>.draw(cpudata<?php echo $i ?>, options);
        <?php }; ?>

    }

</script>


</body>
</html>