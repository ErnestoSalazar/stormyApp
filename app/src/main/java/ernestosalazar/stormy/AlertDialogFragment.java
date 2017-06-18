package ernestosalazar.stormy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by soygo on 14/06/2017.
 */

// Creamos una clase que extiende de DialogFrgment para poder usar sus metodos
public class AlertDialogFragment extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        Context context = getActivity(); // al estar en una clase fuera de contexto DialogFragment tiene un metodo que obtiene
                                         // la activity de donde mandamos a llamar para obtener su contexto

        AlertDialog.Builder builder = new AlertDialog.Builder(context); // instancia de AlertDialog donde construimos sus configuraciones
        builder.setTitle(R.string.error_title)// damos un titulo  nuestro alert dialog
                .setMessage(R.string.error_message) // damos un mensaje nuestro alert dialog
                .setPositiveButton(R.string.error_ok_button_text,null); // Existen botones positivos, negativos y neutrales

        AlertDialog dialog = builder.create(); // ahora creamos el AlertDialog que sera desplegado

        return dialog;
    }
}
