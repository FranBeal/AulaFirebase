package com.example.aulafirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.aulafirebase.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    val bd = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        credentialManager = CredentialManager.create(this)

        binding.btIncluir.setOnClickListener() {
            btIncluirOnClick()
        }
        binding.btAlterar.setOnClickListener() {
            btAlterarOnClick()
        }
        binding.btExcluir.setOnClickListener() {
            btExcluirOnClick()
        }

        binding.btPesquisar.setOnClickListener() {
            btPesquisarOnClick()
        }

        binding.btListar.setOnClickListener() {
            btListaOnClick()
        }

        binding.btSair.setOnClickListener() {
            signOut()
        }
    }

    private fun signOut() {
        auth.signOut()

        lifecycleScope.launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                Log.e("Logout", "Limpando as credenciais do usuário")

                Toast.makeText(
                    this@MainActivity, "Logout realizado com sucesso.",
                    Toast.LENGTH_SHORT
                ).show()

                navigateToLoginScreen()
            } catch (e: ClearCredentialException) {
                     Log.e("Logout", "Não foi possível limpar as credenciais do usuário:" +
                             " ${ e.localizedMessage }")
                     navigateToLoginScreen ()
            } catch (e: Exception) {
                Log.e("Logout", "Erro desconhecido no logout: ${e.localizedMessage}")
                navigateToLoginScreen()
            }
        }
    }

    // 5. Função de navegação
    private fun navigateToLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun btExcluirOnClick() {
        bd.collection("Pessoa")
            .document(binding.etCodigo.text.toString()) //codigo do documento
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Exclusão realizada com sucesso.",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Erro ao excluir: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun btAlterarOnClick() {
        val codigoStr = binding.etCodigo.text.toString().trim()
        val nome = binding.etNome.text.toString().trim()
        val telefone = binding.etTelefone.text.toString().trim()

        //validar
        if (codigoStr.isEmpty() ||
            nome.isEmpty() ||
            telefone.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Todos os campos devem ser preenchidos.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            val codigoInt: Int
            try {
                codigoInt = codigoStr.toInt()

                //Pegar os dados para enviar para o banco
                val pessoaUpdate = hashMapOf<String, Any>(
                    "nome" to nome,
                    "telefone" to telefone
                )

                //Criar o documento
                bd.collection("Pessoa")
                    .document(codigoStr) //codigo do documento
                    .update(pessoaUpdate)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Alteração realizada com sucesso.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Erro ao alterar: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

            } catch (e: NumberFormatException) {
                Toast.makeText(
                    this,
                    "O código deve ser um número.",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    private fun btPesquisarOnClick() {
        val codigoStr = binding.etCodigo.text.toString()
        if (codigoStr.isEmpty()) {
            Toast.makeText(
                this,
                "O campo código é obrigatório.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            val codigoInt: Int
            try {
                codigoInt = codigoStr.toInt()
                bd.collection("Pessoa")
                    .whereEqualTo("codigo", codigoInt)
                    .get()
                    .addOnSuccessListener { result ->
                        if (result.isEmpty) {
                            Toast.makeText(
                                this,
                                "Registro não encontrado",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.etNome.text.clear()
                            binding.etTelefone.text.clear()
                        } else {
                            val registro = result.elementAt(0).data
                            binding.etNome.setText(registro?.get("nome").toString())
                            binding.etTelefone.setText(registro?.get("telefone").toString())
                            Toast.makeText(
                                this,
                                "Registro encontrado: ${registro.get("nome")}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Erro ao pesquisar: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }


            } catch (e: NumberFormatException) {
                Toast.makeText(
                    this,
                    "O código deve ser um número.",
                    Toast.LENGTH_LONG
                ).show()
                binding.etNome.text.clear()
                binding.etTelefone.text.clear()
                return
            }
        }
    }

    private fun btListaOnClick() {
        val saida = StringBuilder()

        bd.collection("Pessoa")
            .get() //recupera tudo
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    saida.append("Nenhum registro encontrado")
                } else {
                    for (document in result) {
                        saida.append(
                            "${document.data.get("nome")} " +
                                    "- ${document.data.get("telefone")}\n"
                        )
                    }
                }
                AlertDialog.Builder(this)
                    .setTitle("Lista de Pessoas")
                    .setMessage(saida.toString())
                    .setPositiveButton("Ok", null)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Erro ao lista: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun btIncluirOnClick() {
        val codigoStr = binding.etCodigo.text.toString().trim()
        val nome = binding.etNome.text.toString().trim()
        val telefone = binding.etTelefone.text.toString().trim()

        //validar
        if (codigoStr.isEmpty() ||
            nome.isEmpty() ||
            telefone.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Todos os campos devem ser preenchidos.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            val codigoInt: Int
            try {
                codigoInt = codigoStr.toInt()

                //Pegar os dados para enviar para o banco
                val pessoa = hashMapOf(
                    "codigo" to codigoInt,
                    "nome" to nome,
                    "telefone" to telefone
                )

                //Criar o documento
                bd.collection("Pessoa")
                    .document(codigoStr) //codigo do documento
                    .set(pessoa)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Inclusão realizada com sucesso.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Erro ao incluir: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

            } catch (e: NumberFormatException) {
                Toast.makeText(
                    this,
                    "O código deve ser um número.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}