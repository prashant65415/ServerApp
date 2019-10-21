# ServerApp
Android application, send input to server and return response

Please install both ClientApp and ServerApp into the android emulator or device.

In the ClientApp, we only take userinput and send it to ServerApp. After getting the result, we display it in popup.
In the ServerApp, we send the input received from ClientApp to server and get the response as json file. Then we parse the
json file to get the response string. We send it to ClientApp.


In the ClientApp, we check for permissions and call the service from ServerApp. The Service in ServerApp send the input to 
server in AsyncTask. After receiving the response, we parse the json. The result is send to ClientApp using Broadcast Receiver.
We register Broadcast Receiver in ClientApp, and show thw received result in AletDialog.

There are other ways to implement this like:
->Call StartActivityForResult in clientApp to call ServerApp activity. Then call the server in AsynTask and send the result
back to ClientApp. Implement OnActivityResult in ClientApp to receive the result. The problem with this approach is, when we 
call StartActivityForResult, ServerActivity is shown and it is an interruption in UI.

Implemented test case in MainActivityTest.java file of ClientApp Application.

Connecting to server and getting the response should be performed in background as Service. And then send the result to ClientApp.
