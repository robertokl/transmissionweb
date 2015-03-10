package androidapps.robertokl.transmissionweb.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.view.View;
import android.widget.EditText;

import androidapps.robertokl.transmissionweb.R;
import androidapps.robertokl.transmissionweb.db.RuleContentProvider;

public class NewRuleFragment extends DialogFragment {
    private View view;
    public static final int ADD_RULE = 1;
    public static final int DELETE_RULE = 2;
    public static final int EDIT_RULE = 3;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.fragment_new_rule, null);

        final Bundle args = getArguments();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        switch (args.getInt("action")) {
            case ADD_RULE:
                dialogBuilder.setTitle("Add Rule");
                dialogBuilder.setView(view);
                dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContentValues values = new ContentValues();
                        values.put("rule", ((EditText) view.findViewById(R.id.txtNewRule)).getText().toString());
                        values.put("folder", ((EditText) view.findViewById(R.id.txtNewFolder)).getText().toString());

                        getActivity().getContentResolver().insert(RuleContentProvider.CONTENT_URI, values);
                        getActivity().getSupportLoaderManager().restartLoader(0, null, (LoaderManager.LoaderCallbacks<Cursor>) getActivity());
                    }
                });
                break;
            case DELETE_RULE:
                dialogBuilder.setTitle("Delete Rule");
                dialogBuilder.setMessage("Are you sure you want to delete '" + args.get("rule_name") + "'?");
                dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = RuleContentProvider.CONTENT_URI;
                        String pathSegment = Integer.toString((int) args.getLong("rule_id"));
                        uri = Uri.withAppendedPath(uri, pathSegment);

                        getActivity().getContentResolver().delete(uri, null, null);
                        getActivity().getSupportLoaderManager().restartLoader(0, null, (LoaderManager.LoaderCallbacks<Cursor>) getActivity());
                    }
                });
                break;
            case EDIT_RULE:
                dialogBuilder.setTitle("Edit Rule");
                dialogBuilder.setView(view);
                ((EditText) view.findViewById(R.id.txtNewRule)).setText(args.getString("rule_name"));
                ((EditText) view.findViewById(R.id.txtNewFolder)).setText(args.getString("rule_folder"));
                dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = RuleContentProvider.CONTENT_URI;
                        String pathSegment = Integer.toString((int) args.getLong("rule_id"));
                        uri = Uri.withAppendedPath(uri, pathSegment);

                        ContentValues values = new ContentValues(2);
                        values.put("rule", ((EditText) view.findViewById(R.id.txtNewRule)).getText().toString());
                        values.put("folder", ((EditText) view.findViewById(R.id.txtNewFolder)).getText().toString());

                        getActivity().getContentResolver().update(uri, values, null, null);
                        getActivity().getSupportLoaderManager().restartLoader(0, null, (LoaderManager.LoaderCallbacks<Cursor>) getActivity());
                    }
                });
                break;

        }
        dialogBuilder.setNegativeButton("Cancel", null);

        return dialogBuilder.create();
    }

}
