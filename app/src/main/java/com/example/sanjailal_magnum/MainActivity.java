package com.example.sanjailal_magnum;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.example.sanjailal_magnum.sampledata.Message;
import com.example.sanjailal_magnum.sampledata.MessagesFixtures;
import com.fivemin.chief.nonetworklibrary.networkBroadcast.NoNet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.Objects;



public class MainActivity extends DemoMessagesActivity
        implements MessageInput.InputListener,
        MessageInput.AttachmentsListener,
        MessageInput.TypingListener, View.OnClickListener {
    int noOfClick;
    private NoNet mNoNet;
    private TextView initialtext;

    public static void open(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    private MessagesList messagesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.messagesList = findViewById(R.id.messagesList);
        initAdapter();
        MessageInput input = findViewById(R.id.input);
        input.setInputListener(this);
        input.setTypingListener(this);
        input.setAttachmentsListener(this);
        RelativeLayout relativeLayout = findViewById(R.id.rellayout);
        relativeLayout.setOnClickListener(this);
        noOfClick = 1;
        initialtext = findViewById(R.id.editText);
        initialtext.setOnClickListener(this);
        FragmentManager fm = getSupportFragmentManager();
        mNoNet = new NoNet();
        mNoNet.initNoNet(this, fm);
    }

    @Override
    protected void onResume() {
        mNoNet.RegisterNoNet();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mNoNet.unRegisterNoNet();
        super.onPause();
    }


    private void retrieveData(final int noOfClick) {    // Retrieving from database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d("retrieve", document.getId() + " => " + document.getString("msg1"));
                                dotask(document.getString("msg" + noOfClick));
                            }
                        } else {
                            Log.w("retrieve", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void dotask(String text) {     //adding to messagesAdapter
        super.messagesAdapter.addToStart(
                MessagesFixtures.getTextMessage(text), true);
    }

    private void retrieveImage() {
        super.messagesAdapter.addToStart(
                MessagesFixtures.getImageMessage(), true);
    }


    private void initAdapter() {      // onClick on any view takes place
        super.messagesAdapter = new MessagesListAdapter<>(super.senderId, super.imageLoader);
        super.messagesAdapter.enableSelectionMode(this);
        //Change the registerViewClickListener to any id to make it ocClick
        super.messagesAdapter.registerViewClickListener(R.id.messageText,
                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
                    @Override
                    public void onMessageViewClick(View view, Message message) {
                        onTouch();
                    }
                });
        super.messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
                    @Override
                    public void onMessageViewClick(View view, Message message) {
                        onTouch();
                        AppUtils.showToast(MainActivity.this,
                                message.getUser().getName() + " avatar click",
                                false);
                    }
                });
        this.messagesList.setAdapter(super.messagesAdapter);
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        super.messagesAdapter.addToStart(
                MessagesFixtures.getTextMessage(input.toString()), true);
        return true;
    }

    @Override
    public void onAddAttachments() {   // to send our file as an attachment
        super.messagesAdapter.addToStart(
                MessagesFixtures.getImageMessage(), true);
    }


    @Override
    public void onStartTyping() {
        Log.v("Typing listener", "You are the one typing");
    }

    @Override
    public void onStopTyping() {
        Log.v("Typing listener", "You stopped. Why?");
    }

    @Override
    public void onSelectionChanged(int count) {

    }

    //available for future use
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void onTouch() {
        if (noOfClick == 1) {
            initialtext.setVisibility(View.GONE);
        }
        if (noOfClick % 5 == 0) {
            retrieveImage();
        } else {
            AppUtils.showToast(getApplicationContext(), "Retrieving text", false);
            retrieveData(noOfClick);
        }
        noOfClick++;
    }

    @Override
    public void onClick(View v) {  // onClickListener for the layout
        if (v.getId() == R.id.rellayout) {
            onTouch();
        } else if (v.getId() == R.id.editText) {
            initialtext.setVisibility(View.GONE);
            onTouch();
        }

        // Check messagefixtures.java for more detail

    }


}