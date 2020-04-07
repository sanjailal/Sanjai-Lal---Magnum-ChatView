package com.example.sanjailal_magnum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.sanjailal_magnum.sampledata.MessagesFixtures;
import com.example.sanjailal_magnum.sampledata.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.net.InetAddress;
import java.util.Objects;


public class MainActivity extends DemoMessagesActivity
        implements MessageInput.InputListener,
        MessageInput.AttachmentsListener,
        MessageInput.TypingListener, View.OnClickListener {
    Button retrieveButton;
    int noOfClick;

    public static void open(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    private MessagesList messagesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.messagesList = (MessagesList) findViewById(R.id.messagesList);
        initAdapter();
        MessageInput input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(this);
        input.setTypingListener(this);
        input.setAttachmentsListener(this);
        messagesList.setOnClickListener(this);
        retrieveButton=(Button) findViewById(R.id.retrieve);
        retrieveButton.setOnClickListener(this);
        noOfClick=1;

    }

    private void retrieveData(final int noOfClick) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d("retrieve", document.getId() + " => " + document.getString("msg1"));
                                dotask(document.getString("msg"+noOfClick));
                            }
                        } else {
                            Log.w("retrieve", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
    private void dotask(String text){
        super.messagesAdapter.addToStart(
                MessagesFixtures.getTextMessage(text), true);
    }
    private void retrieveImage(){
        super.messagesAdapter.addToStart(
                MessagesFixtures.getImageMessage(), true);
    }


    private void initAdapter() {
        super.messagesAdapter = new MessagesListAdapter<>(super.senderId, super.imageLoader);
        super.messagesAdapter.enableSelectionMode(this);
        //super.messagesAdapter.setLoadMoreListener(this); Can be used to popuate a lot of messages
        super.messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
                    @Override
                    public void onMessageViewClick(View view, Message message) {
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
    public void onAddAttachments() {
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
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.retrieve){
            if(isInternetAvailable()) {
                Toast.makeText(getApplicationContext(), "True", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplicationContext(), "False", Toast.LENGTH_LONG).show();
            }
            if(noOfClick%5==0){
                retrieveImage();
            }
            else{
                retrieveData(noOfClick);
            }
            noOfClick++;
        }
        else if(v.getId()==R.id.messagesList){
            Toast.makeText(getApplicationContext(),"anywhere else",Toast.LENGTH_LONG).show();
        }

    }
}
