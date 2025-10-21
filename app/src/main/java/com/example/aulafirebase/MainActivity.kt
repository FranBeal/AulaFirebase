package com.example.aulafirebase

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aulafirebase.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    val bd = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btIncluir.setOnClickListener(){
            btIncluirOnClick()
        }
        binding.btAlterar.setOnClickListener(){}
        binding.btExcluir.setOnClickListener(){}
        binding.btPesquisar.setOnClickListener(){}
        binding.btListar.setOnClickListener(){
            btListaOnClick()
        }


    }

    private fun btListaOnClick() {
        val saida = StringBuilder()

        bd.collection("Pessoa")
            .get() //recupera tudo
            .addOnSuccessListener {  result ->
                if(result.isEmpty){
                    saida.append("Nenhum registro encontrado")
                }else{
                    for(document in result){
                        saida.append("${document.data.get("nome")} " +
                                "- ${document.data.get("telefone")}\n")
                    }
                }
                AlertDialog.Builder(this)
                    .setTitle("Lista de Pessoas")
                    .setMessage(saida.toString())
                    .setPositiveButton("Ok", null)
                    .show()
            }
            .addOnFailureListener{ e ->
                Toast.makeText(this,
                    "Erro ao lista: ${e.localizedMessage}",
                    Toast.LENGTH_LONG).show()
            }
    }

    private fun btIncluirOnClick() {
        val codigoStr = binding.etCodigo.text.toString().trim()
        val nome = binding.etNome.text.toString().trim()
        val telefone = binding.etTelefone.text.toString().trim()

        //validar
        if(codigoStr.isEmpty() ||
            nome.isEmpty() ||
            telefone.isEmpty()){
            Toast.makeText(this,
                "Todos os campos devem ser preenchidos.",
                Toast.LENGTH_LONG).show()
        }else{
            val codigoInt: Int
            try{
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
                        Toast.makeText(this,
                            "Inclusão realizada com sucesso.",
                            Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener{ e ->
                        Toast.makeText(this,
                            "Erro ao incluir: ${e.localizedMessage}",
                            Toast.LENGTH_LONG).show()
                    }

            }catch (e: NumberFormatException){
                Toast.makeText(this,
                    "O código deve ser um número.",
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}