link=$(curl --upload-file target/RetroServer.jar https://transfer.sh)
server="https://retrorealms.net/maindeploy.php?link=$link&pass=RetroDisPatch691324&type=main"
curl -X GET $server
