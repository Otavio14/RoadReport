package com.otavio14.roadreport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;

public class CadastroActivity extends AppCompatActivity {
    EditText editNome, editCpf, editTelefone, editSenha, editEmail;
    Button buttonCadastrar, buttonLogin;

    // Lista de dados a serem inseridos
    public Map<String, Object> usuario = new HashMap<>();

    //Acesso a intância do Cloud Firestore
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    public FirebaseAuth mAuth;

    public static final String FORMAT_CPF = "###.###.###-##";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro);

        //bloqueia o modo escuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        editNome = findViewById(R.id.editNome);
        editCpf = findViewById(R.id.editCpf);
        editTelefone = findViewById(R.id.editTelefone);
        editSenha = findViewById(R.id.editSenha);
        editEmail = findViewById(R.id.editEmail);
        buttonCadastrar = findViewById(R.id.buttonCadastrar);
        buttonLogin = findViewById(R.id.buttonLogin);

        //Instance da conta
        mAuth = FirebaseAuth.getInstance();

        //Formata o número de telefone
        editTelefone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        editCpf.addTextChangedListener(new TextWatcher() {
            boolean isUpdating;
            String old = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String str = unmask(s.toString());
                StringBuilder mascara = new StringBuilder();
                if (isUpdating) {
                    old = str;
                    isUpdating = false;
                    return;
                }
                int i = 0;
                for (final char m : FORMAT_CPF.toCharArray()) {
                    if (m != '#' && str.length() > old.length()) {
                        mascara.append(m);
                        continue;
                    }
                    try {
                        mascara.append(str.charAt(i));
                    } catch (final Exception e) {
                        break;
                    }
                    i++;
                }
                isUpdating = true;
                editCpf.setText(mascara.toString());
                editCpf.setSelection(mascara.length());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        buttonCadastrar.setOnClickListener(v -> {
            //Verifica se os campos estão preenchidos
            if (TextUtils.isEmpty(editCpf.getText().toString()) ||
                    TextUtils.isEmpty(editEmail.getText().toString()) ||
                    TextUtils.isEmpty(editNome.getText().toString()) ||
                    TextUtils.isEmpty(editSenha.getText().toString()) ||
                    TextUtils.isEmpty(editTelefone.getText().toString()) ||
                    editSenha.getText().toString().length() < 6 ||
                    !validarCpf(unmask(editCpf.getText().toString()))) {
                Toast.makeText(getApplicationContext(), "Dados incorretos", Toast.LENGTH_LONG).show();
            } else {
                cadastrarConta();
            }
        });

        buttonLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Valida o CPF inserido
     *
     * @param cpf - cpf inserido
     * @return - retorna se é valido ou não
     */
    private boolean validarCpf(String cpf) {
        if (cpf.equals("00000000000") ||
                cpf.equals("11111111111") ||
                cpf.equals("22222222222") || cpf.equals("33333333333") ||
                cpf.equals("44444444444") || cpf.equals("55555555555") ||
                cpf.equals("66666666666") || cpf.equals("77777777777") ||
                cpf.equals("88888888888") || cpf.equals("99999999999") ||
                (cpf.length() != 11))
            return (false);

        char dig10, dig11;
        int sm, i, r, num, peso;

        // "try" - protege o codigo para eventuais erros de conversao de tipo (int)
        try {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 10;
            for (i = 0; i < 9; i++) {
                // converte o i-esimo caractere do CPF em um numero:
                // por exemplo, transforma o caractere '0' no inteiro 0
                // (48 eh a posicao de '0' na tabela ASCII)
                num = cpf.charAt(i) - 48;
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig10 = '0';
            else dig10 = (char) (r + 48); // converte no respectivo caractere numerico

            // Calculo do 2o. Digito Verificador
            sm = 0;
            peso = 11;
            for (i = 0; i < 10; i++) {
                num = cpf.charAt(i) - 48;
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig11 = '0';
            else dig11 = (char) (r + 48);

            // Verifica se os digitos calculados conferem com os digitos informados.
            return (dig10 == cpf.charAt(9)) && (dig11 == cpf.charAt(10));
        } catch (InputMismatchException erro) {
            return (false);
        }
    }

    /**
     * Retira a máscara do CPF
     *
     * @param s - CPF atual
     * @return - retorna o CPF sem a máscara
     */
    public static String unmask(final String s) {
        return s.replaceAll("[.]", "").replaceAll("[-]", "").replaceAll("[/]", "").replaceAll("[(]", "").replaceAll("[ ]", "").replaceAll("[:]", "").replaceAll("[)]", "");
    }

    /**
     * Cadastra a conta na autenticação do Firebase
     */
    private void cadastrarConta() {
        mAuth.createUserWithEmailAndPassword(editEmail.getText().toString(), editSenha.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("teste", "createUserWithEmail:success");
                        cadastrarDados();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d("teste", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Desabilita o botão voltar
    @Override
    public void onBackPressed() {
    }

    /**
     * Insere os dados do cadastro no banco de dados
     */
    private void cadastrarDados() {
        usuario.put("administrador", false);
        usuario.put("cpf", editCpf.getText().toString());
        usuario.put("email", editEmail.getText().toString());
        usuario.put("nome", editNome.getText().toString());
        usuario.put("senha", editSenha.getText().toString());
        usuario.put("telefone", editTelefone.getText().toString());
        database.collection("usuario")
                .add(usuario)
                .addOnSuccessListener(documentReference -> {
                    //Inicia uma sessão de login
                    SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences", Context.MODE_PRIVATE);
                    //Insere os dados da sessão
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("email_key", editEmail.getText().toString());
                    editor.putString("senha_key", editSenha.getText().toString());
                    editor.putBoolean("administrador_key", false);
                    editor.putString("nome_key", editNome.getText().toString() + " ");
                    editor.putString("idUsuario_key", documentReference.getId());
                    editor.apply();
                    Intent intent = new Intent(CadastroActivity.this, MapsActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Log.w("teste", "Error adding document", e));
    }
}