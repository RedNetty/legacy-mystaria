link=$(curl --upload-file target/RetroServer.jar https://transfer.sh)
server="https://retrorealms.net/maindeploy.php?link=$link&pass=RetroDisPatch691324&type=dev"
curl -X GET $server
