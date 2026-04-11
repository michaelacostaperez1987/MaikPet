<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: text/plain; charset=utf-8');

echo "=== Testing DB Connection ===\n\n";

require_once 'config.php';

echo "DB Host: " . DB_HOST . "\n";
echo "DB User: " . DB_USER . "\n";
echo "DB Name: " . DB_NAME . "\n";

$conn = @new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME, DB_PORT);

if ($conn->connect_error) {
    echo "\n❌ CONNECTION ERROR:\n";
    echo "Error: " . $conn->connect_error . "\n";
    echo "Errno: " . $conn->connect_errno . "\n";
} else {
    echo "\n✅ CONNECTION SUCCESS\n";
    echo "Server Info: " . $conn->server_info . "\n";
    
    // Test query
    echo "\n=== Testing INSERT ===\n";
    $stmt = $conn->prepare("SELECT 1 as test");
    if ($stmt) {
        $stmt->execute();
        $result = $stmt->get_result();
        $row = $result->fetch_assoc();
        echo "✅ Prepare works: " . $row['test'] . "\n";
        $stmt->close();
    } else {
        echo "❌ Prepare error: " . $conn->error . "\n";
    }
    
    // Test mascotas table
    echo "\n=== Testing mascotas table ===\n";
    $result = $conn->query("DESCRIBE mascotas");
    if ($result) {
        echo "✅ mascotas table exists\n";
        $hasImagen = false;
        while ($row = $result->fetch_assoc()) {
            if ($row['Field'] === 'imagen') {
                $hasImagen = true;
                echo "   imagen type: " . $row['Type'] . "\n";
            }
        }
        if (!$hasImagen) {
            echo "   ⚠️ imagen column NOT found\n";
        }
    } else {
        echo "❌ Error: " . $conn->error . "\n";
    }
    
    $conn->close();
}
?>
