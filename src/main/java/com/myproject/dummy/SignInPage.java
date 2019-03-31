package com.myproject.dummy;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
//import android.os.IBinder;
//import android.os.Binder;
import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
//import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchAuth;
import com.mongodb.stitch.android.core.auth.StitchAuthListener;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.android.services.mongodb.remote.SyncFindIterable;
//import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.auth.providers.facebook.FacebookCredential;
import com.mongodb.stitch.core.auth.providers.google.GoogleCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.sync.ChangeEventListener;
import com.mongodb.stitch.core.services.mongodb.remote.sync.DefaultSyncConflictResolvers;
import com.mongodb.stitch.core.services.mongodb.remote.sync.ErrorListener;
import com.mongodb.stitch.core.services.mongodb.remote.sync.internal.ChangeEvent;

import org.bson.BsonValue;
import org.bson.Document;
/*
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
*/

/*import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;*/

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.android.gms.auth.api.Auth.GOOGLE_SIGN_IN_API;
import static com.google.android.gms.auth.api.Auth.GoogleSignInApi;

public class SignInPage extends AppCompatActivity {

    private static final String TAG = "EmbryoPhase2";
    private static final int RC_SIGN_IN = 421;

    private CallbackManager _callbackManager;
    private GoogleApiClient _googleApiClient;
    private StitchAppClient _client;
    private RemoteMongoClient _mongoClient;
    private RemoteMongoCollection _remoteCollection;
    private DeviceListAdapter2 _devAdapter;
    private Handler _handler;
    private Runnable _refresher;
    //private String clientId;
    //private MqttAndroidClient client;
    //private MyMqttService _myMqttService;
    private RecyclerView listView;
    private boolean _fbInitOnce;

    private EditText phonenumber;
    private EditText password;
    private Button signinbtn;
    //private Context context = this.getApplicationContext() ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _handler = new Handler();
        _refresher = new ListRefresher(this);

        this._client = Stitch.getDefaultAppClient();
        this._client.getAuth().addAuthListener(new MyAuthListener(this));
        _mongoClient = this._client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
        _remoteCollection = _mongoClient.getDatabase("alpha").getCollection("people");
        _remoteCollection.sync().configure(
                DefaultSyncConflictResolvers.remoteWins(),
                new MyUpdateListener(),
                new MyErrorListener()
        );

        setupLogin();
    }


    private static class MyAuthListener implements StitchAuthListener {
        private WeakReference<SignInPage> _main;
        private StitchUser _user;

        public MyAuthListener(final SignInPage activity) {
            _main = new WeakReference<>(activity);
        }

        @Override
        public void onAuthEvent(final StitchAuth auth) {
            if (auth.isLoggedIn() && _user == null) {
                Log.d(TAG, "Logged into Stitch");
                _user = auth.getUser();
                return;
            }

            if (!auth.isLoggedIn() && _user != null) {
                _user = null;
                onLogout();
            }

        }

        public void onLogout() {
            final SignInPage activity = _main.get();

            final List<Task<Void>> futures = new ArrayList<>();
            if (activity != null) {
                activity._handler.removeCallbacks(activity._refresher);

                if (activity._googleApiClient != null) {
                    final TaskCompletionSource<Void> future = new TaskCompletionSource<>();
                    GoogleSignInApi.signOut(
                            activity._googleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull final Status ignored) {
                            future.setResult(null);
                        }
                    });
                    futures.add(future.getTask());
                }

                if (activity._fbInitOnce) {
                    LoginManager.getInstance().logOut();
                    activity._fbInitOnce = false;
                }

                Tasks.whenAll(futures).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull final Task<Void> ignored) {
                        activity.setupLogin();
                    }
                });
            }
        }
    }

    private static class ListRefresher implements Runnable {

        private WeakReference<SignInPage> _main;

        private ListRefresher(final SignInPage activity) {
            _main = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            final SignInPage activity = _main.get();
            if (activity != null && activity._client.getAuth().isLoggedIn()) {
                activity.refreshList();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            final GoogleSignInResult result = GoogleSignInApi.getSignInResultFromIntent(data);
            handleGooglSignInResult(result);
            return;
        }

        if (_callbackManager != null) {
            _callbackManager.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Log.e(TAG, "Nowhere to send activity result for ourselves");
    }

    private void handleGooglSignInResult(final GoogleSignInResult result) {
        if (result == null) {
            Log.e(TAG, "Got a null GoogleSignInResult");
            return;
        }

        Log.d(TAG, "handleGooglSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            try {
                final GoogleCredential googleCredential = new GoogleCredential(result.getSignInAccount().getServerAuthCode());
                _client.getAuth().loginWithCredential(googleCredential).addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                    @Override
                    public void onComplete(@NonNull final Task<StitchUser> task) {
                        if (task.isSuccessful()) {
                            initEmbryoView();
                        } else {
                            Log.e(TAG, "Error logging in with Google", task.getException());
                        }
                    }
                });
            } catch (NullPointerException e) {
                Log.e("NPE", "NULL POINTER EXCEPTION at line 199");
            }

        } else {
            Toast.makeText(SignInPage.this, "Failed to log on using Google. Result: " + result.getStatus(), Toast.LENGTH_LONG).show();
        }
    }



    private void initEmbryoView() {
        setContentView(R.layout.device_list_two);




        /*clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883",
                                      clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }*/


        _devAdapter = new DeviceListAdapter2(
                this,
                R.layout.device,
                new ArrayList<Device>(),
                _remoteCollection
        );
        listView = findViewById(R.id.deviceList2);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(_devAdapter);
        //((ListView) findViewById(R.id.deviceList)).setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        findViewById(R.id.logout2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View ignored) {
                _client.getAuth().logout();
            }
        });
        findViewById(R.id.test_mqtt2).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View ignored){
                Intent testing = new Intent(SignInPage.this, MyMqttServiceTest.class);
                startActivity(testing);
            }
        });
        findViewById(R.id.addItem2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View ignored) {
                final AlertDialog.Builder diagBuilder = new AlertDialog.Builder(SignInPage.this);

                final LayoutInflater inflater = SignInPage.this.getLayoutInflater();
                final View view = inflater.inflate(R.layout.add_device, null);
                final EditText Name = view.findViewById(R.id.deviceName);
                final EditText Id = view.findViewById(R.id.deviceId);

                diagBuilder.setView(view);
                diagBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        addDevice(Name.getText().toString(), Id.getText().toString());
                    }
                });
                diagBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        dialogInterface.cancel();
                    }
                });
                diagBuilder.setCancelable(false);
                diagBuilder.create().show();
            }
        });
        _refresher.run();
    }

    private void addDevice(final String devName, final String devId) {
        final Document doc = new Document();
        doc.put("owner_id", _client.getAuth().getUser().getId()); //todo
        doc.put("devicename", devName);
        doc.put("deviceid", devId);
        doc.put("checked", false);

        final Task<RemoteInsertOneResult> res = _remoteCollection.sync().insertOne(doc);
        res.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull Task<RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    refreshList();
                } else {
                    Log.e(TAG, "Error adding device", task.getException());
                }
            }
        });
    }

    private List<Device> convertDocsToDevice(final List<Document> docs) {
        final List<Device> devs = new ArrayList<>(docs.size());
        for (final Document d : docs) {
            devs.add(new Device(d));
        }
        return devs;
    }

    private void refreshList() {
        Document filter = new Document("owner_id", _client.getAuth().getUser().getId());
        SyncFindIterable cursor = _remoteCollection.sync().find(filter).limit(100);
        final ArrayList<Document> docs = new ArrayList<>();
        cursor.into(docs).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                _devAdapter.clear();
                _devAdapter.addAll(convertDocsToDevice(docs));
                _devAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setupLogin() {
        if (_client.getAuth().isLoggedIn()) {
            initEmbryoView();
            return;
        }

        final String facebookAppId = getString(R.string.facebook_app_id);
        final String googleClientId = getString(R.string.google_client_id);

        setContentView(R.layout.login_page);

        // If there is a valid Facebook App ID defined in strings.xml, offer Facebook as a login option.
        if (!facebookAppId.equals("TBD")) {
            findViewById(R.id.facebook_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View ignored) {

                    // Check if already logged in
                    if (AccessToken.getCurrentAccessToken() != null) {
                        final FacebookCredential fbCredential = new FacebookCredential(AccessToken.getCurrentAccessToken().getToken());
                        _client.getAuth().loginWithCredential(fbCredential).addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                            @Override
                            public void onComplete(@NonNull final Task<StitchUser> task) {
                                if (task.isSuccessful()) {
                                    _fbInitOnce = true;
                                    initEmbryoView();
                                } else {
                                    Log.e(TAG, "Error logging in with Facebook", task.getException());
                                }
                            }
                        });
                        //return;
                    }

                    _callbackManager = CallbackManager.Factory.create();
                    LoginManager.getInstance().registerCallback(_callbackManager, new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            final FacebookCredential fbCredential = new FacebookCredential(AccessToken.getCurrentAccessToken().getToken());
                            _client.getAuth().loginWithCredential(fbCredential).addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                                @Override
                                public void onComplete(@NonNull final Task<StitchUser> task) {
                                    if (task.isSuccessful()) {
                                        _fbInitOnce = true;
                                        initEmbryoView();
                                    } else {
                                        Log.e(TAG, "Error logging in with Facebook", task.getException());
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(SignInPage.this, "Facebook logon was " +
                                    "cancelled.", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(final FacebookException exception) {
                            Toast.makeText(SignInPage.this, "Failed to logon with " +
                                    "Facebook. Result: " + exception.toString(), Toast.LENGTH_LONG).show();

                        }
                    });
                    LoginManager.getInstance().logInWithReadPermissions(
                            SignInPage.this, Arrays.asList("public_profile"));
                }
            });
            //findViewById(R.id.fb_login_button_frame).setVisibility(View.VISIBLE);
        }

        // If there is a valid Google Client ID defined in strings.xml, offer Google as a login option.
        if (!googleClientId.equals("TBD")) {
            final GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestServerAuthCode(googleClientId, false);
            final GoogleSignInOptions gso = gsoBuilder.build();

            if (_googleApiClient != null) {
                _googleApiClient.stopAutoManage(SignInPage.this);
                _googleApiClient.disconnect();
            }

            _googleApiClient = new GoogleApiClient.Builder(SignInPage.this)
                    .enableAutoManage(SignInPage.this, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Log.e(TAG, "Error connecting to google: " + connectionResult.getErrorMessage());
                        }
                    })
                    .addApi(GOOGLE_SIGN_IN_API, gso)
                    .build();

            findViewById(R.id.google_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View ignored) {
                    final Intent signInIntent =
                            GoogleSignInApi.getSignInIntent(_googleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });
            //findViewById(R.id.google_login_button).setVisibility(View.VISIBLE);
        }

        // Anonymous login
        /*findViewById(R.id.anonymous_login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View ignored) {
                _client.getAuth().loginWithCredential(new AnonymousCredential()).addOnCompleteListener(new OnCompleteListener<StitchUser>() {
                    @Override
                    public void onComplete(@NonNull final Task<StitchUser> task) {
                        if (task.isSuccessful()) {
                            initEmbryoView();
                        } else {
                            Log.e(TAG, "Error logging in anonymously", task.getException());
                        }
                    }
                });
            }
        });
        findViewById(R.id.anonymous_login_button).setVisibility(View.VISIBLE);*/

        //TODO: Provide code for login using phonenumber and password
        phonenumber = findViewById(R.id.phone);
        password = findViewById(R.id.pwd);
        signinbtn = findViewById(R.id.signinbtn);
        signinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View ignored) {

                //TODO: Validation and Login code
                Log.d("Msg", "This method is not yet set. Apologies!");
                Log.d("JustChecking", "Phone number Entered is " + phonenumber.getText().toString());
                Log.d("JustChecking", "Password Entered is " + password.getText().toString());
                if (phonenumber.getText().toString().equals("9881756564") && password.getText().toString().equals("YOUDIDIT!")) {
                    initEmbryoView();
                }
            }
        });

    }

    private class MyUpdateListener implements ChangeEventListener<Document> {
        @Override
        public void onEvent(final BsonValue documentId, final ChangeEvent<Document> event) {

            // Is this change coming from local or remote?

            if (event.hasUncommittedWrites()) { //change initiated on the device
                Log.d("STITCH", "Local change to document " + documentId);

                // Add to list of pending changes so we don't end up with a race condition
                _devAdapter.addToPending(documentId);

            } else { //remote change
                Log.d("STITCH", "Remote change to document " + documentId);

                if (!_devAdapter.pendingContains(documentId)) refreshList();
                else _devAdapter.removeFromPending(documentId);
            }
        }
    }

    private class MyErrorListener implements ErrorListener {
        @Override
        public void onError(BsonValue documentId, Exception error) {
            Log.e("Stitch", error.getLocalizedMessage());

            Set<BsonValue> docsThatNeedToBeFixed = _remoteCollection.sync().getPausedDocumentIds();
            for (BsonValue doc_id : docsThatNeedToBeFixed) {
                // TODO:
                // Add your logic to inform the user.
                // When errors have been resolved, call
                _remoteCollection.sync().resumeSyncForDocument(doc_id);
            }
        }
    }
}
