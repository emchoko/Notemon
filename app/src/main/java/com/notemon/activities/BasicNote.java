package com.notemon.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.notemon.R;
import com.notemon.fragments.MediaNoteFragment;
import com.notemon.fragments.TextNoteFragment;
import com.notemon.fragments.TodoListNoteFragment;
import com.notemon.helpers.Constants;
import com.notemon.helpers.DialogBuilder;
import com.notemon.models.BaseNote;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BasicNote extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    String title;
    String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_note);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        replaceFragment(getIntent());
    }

    //TODO: replace with the title of the note and send the needed objects
    private void replaceFragment(Intent intent) {
        FragmentManager manager = getFragmentManager();

        switch (intent.getStringExtra(Constants.NOTE_TYPE)) {
            case Constants.NOTE_TEXT:
                title = intent.getStringExtra(Constants.NOTE_TITLE);
                content = intent.getStringExtra(Constants.NOTE_TEXT_CONTENT);

                toolbar.setTitle(title);

                TextNoteFragment textNoteFragment = new TextNoteFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.NOTE_TEXT_CONTENT, content);
                bundle.putSerializable(Constants.NOTE_TEXT, intent.getSerializableExtra(Constants.NOTE_TEXT));
                textNoteFragment.setArguments(bundle);

                manager.beginTransaction()
                        .replace(R.id.basicNoteFrameLayout, textNoteFragment)
                        .commit();
                break;
            case Constants.NOTE_MEDIA:

                MediaNoteFragment mediaNoteFragment = new MediaNoteFragment();
                Bundle args = new Bundle();
                args.putSerializable(Constants.NOTE_MEDIA, intent.getSerializableExtra(Constants.NOTE_MEDIA));
                mediaNoteFragment.setArguments(args);

                manager.beginTransaction()
                        .replace(R.id.basicNoteFrameLayout, mediaNoteFragment)
                        .commit();
                toolbar.setTitle(intent.getStringExtra(Constants.NOTE_TITLE));
                break;
            case Constants.NOTE_TODO:
                BaseNote note = (BaseNote) intent.getSerializableExtra(Constants.NOTE_TODO);

                TodoListNoteFragment todoFragment = new TodoListNoteFragment();
                Bundle argsTodo = new Bundle();
                argsTodo.putSerializable(Constants.NOTE_TODO, note);
                todoFragment.setArguments(argsTodo);
                manager.beginTransaction()
                        .replace(R.id.basicNoteFrameLayout, todoFragment)
                        .commit();

                toolbar.setTitle(note.getTitle());
                break;
            case Constants.NOTE_VOICE:
                toolbar.setTitle("Voice Note");
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        String msg = "";
        switch (id) {
            case R.id.action_add_reminder:
                msg = "reminder";
                DialogBuilder.addReminder(this);
                break;
            case R.id.action_add_to_project:
                msg = "add to project";
                DialogBuilder.addToProject(this);
                break;
            case R.id.action_share_to_user:
                msg = "share to user";
                DialogBuilder.enterUsername(this);
                break;
            case R.id.action_delete:
                msg = "delete";
                DialogBuilder.promptForDelete(this);
                break;
        }

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        return super.onOptionsItemSelected(item);
    }

}
