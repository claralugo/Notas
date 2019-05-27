package com.example.notas;

import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.base_de_datos.DatabaseHelper;
import com.utils.MyDividerItemDecoration;
import com.utils.RecyclerTouchListener;
import com.vista.NotesAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private NotesAdapter mAdapter;
    private List<Note> noteList= new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noNotestView;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        recyclerView = findViewById(R.id.recycler_view);
        noNotestView = findViewById(R.id.empty_notes_view);

        db = new DatabaseHelper(this);

        noteList.addAll(db.getAllNotes()) ;

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mAdapter = new NotesAdapter(this, noteList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyNotes();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));

    }

    private void createNote (String note){
        long id= db.insertNote(note);

        Note n = db.getNote(id);

        if (n!=null){
            noteList.add(0, n);
            mAdapter.notifyDataSetChanged();
            toggleEmptyNotes();
        }
    }

    private  void updateNote (String note, int position){
        Note n = noteList.get(position);
        n.setNote(note);
        db.updateNote(n);
        noteList.set(position, n);
        mAdapter.notifyItemChanged(position);

        toggleEmptyNotes();

    }

    private void deleteNote(int position){
        db.deleteNote(noteList.get(position));
        noteList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyNotes();
    }

    private void showActionsDialog (final int position){
        CharSequence colors[] = new CharSequence[]{"Editar", "Eliminar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccione opción");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    showNoteDialog(true, noteList.get(position), position);
                }else {
                    deleteNote(position);
                }
            }
        }) ;
        builder.show();
    }

    private void showNoteDialog(final boolean shoulUpdate, final Note note, final int position){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shoulUpdate ? getString(R.string.lbl_new_note_tittle):
                getString(R.string.lbl_edit_note_tittle));

        if (shoulUpdate && note != null){
            inputNote.setText (note.getNote());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shoulUpdate ? "Actualizar" : "Grabar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("Cancelar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });
        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (TextUtils.isEmpty(inputNote.getText().toString())){
                   Toast.makeText(MainActivity.this, "Enter nota!", Toast.LENGTH_SHORT).show();
                   return;
               }          else {
                   alertDialog.dismiss();
               }

               if (shoulUpdate && note != null){
                   updateNote(inputNote.getText().toString(), position);
               }        else {
                   createNote(inputNote.getText().toString());
               }
            }

    });
    }

    private void toggleEmptyNotes(){
        if (db.getNotesCount()>0){
            noNotestView.setVisibility(View.GONE);
        }             else{
            noNotestView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
