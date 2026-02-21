package com.example.cartaocredito

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.ImageView
import android.widget.ViewFlipper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    companion object { private const val TAG = "MainActivity" }

    private var versoVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Pega a vista raiz de forma segura para aplicar insets. Se R.id.main faltar, usa decorView.
        val raiz: View? = try {
            findViewById(R.id.main) ?: window.decorView.rootView
        } catch (e: Exception) {
            Log.w(TAG, "findViewById(R.id.main) falhou, usando decorView: ${e.message}")
            window.decorView.rootView
        }

        Log.d(TAG, "vista raiz (id): ${raiz?.id}")

        raiz?.let { v ->
            ViewCompat.setOnApplyWindowInsetsListener(v) { view, insets ->
                val barrasSistema = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(barrasSistema.left, barrasSistema.top, barrasSistema.right, barrasSistema.bottom)
                insets
            }
        }

        // Comportamento de virar o cartão
        val trocador = findViewById<ViewFlipper>(R.id.cardFlipper)
        // Garante pivot central para rotação 3D correta
        trocador.post {
            val pivotX = trocador.width / 2f
            val pivotY = trocador.height / 2f
            for (i in 0 until trocador.childCount) {
                val filho = trocador.getChildAt(i)
                filho.pivotX = pivotX
                filho.pivotY = pivotY
                // aumenta a distância da câmera para efeito 3D
                val escala = resources.displayMetrics.density
                filho.cameraDistance = 8000 * escala
            }
        }

        // Carrega animadores
        val animEntrada = AnimatorInflater.loadAnimator(this, R.animator.flip_in) as AnimatorSet
        val animSaida = AnimatorInflater.loadAnimator(this, R.animator.flip_out) as AnimatorSet
        animEntrada.interpolator = AccelerateDecelerateInterpolator()
        animSaida.interpolator = AccelerateDecelerateInterpolator()

        trocador.setInAnimation(null)
        trocador.setOutAnimation(null)

        val realizarFlip = {
            val atual = trocador.displayedChild
            val paraMostrar = if (atual == 0) 1 else 0

            val vistaVisivel = trocador.getChildAt(atual)
            val proximaVista = trocador.getChildAt(paraMostrar)

            animSaida.setTarget(vistaVisivel)
            animEntrada.setTarget(proximaVista)
            animSaida.start()
            // Atraso para iniciar animEntrada sincronizado com tempo do animador
            proximaVista.postDelayed({ animEntrada.start() }, 150)

            trocador.showNext()
            versoVisivel = !versoVisivel
            Log.d(TAG, "Cartão virado. versoVisivel=$versoVisivel")
        }

        trocador.setOnClickListener { realizarFlip() }
        findViewById<View>(R.id.include_card_front)?.setOnClickListener { realizarFlip() }
        findViewById<View>(R.id.include_card_back)?.setOnClickListener { realizarFlip() }

        // Sincroniza entradas com visual do cartão
        val edtNumero = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editNumero)
        val edtNome = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editNome)
        val edtValidade = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editValidade)
        val edtCVV = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editCVV)
        val edtAgencia = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editAgencia)
        val edtConta = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editConta)

        // Busca os includes para acessar os views internos com segurança (com fallback para findViewById)
        val includeFrente = findViewById<View?>(R.id.include_card_front)
        val includeVerso = findViewById<View?>(R.id.include_card_back)

        // TextViews na frente (tenta obter dentro do include, senão cai para activity.findViewById)
        val txtNumero = includeFrente?.findViewById<TextView>(R.id.txtNumeroCartao)
            ?: findViewById(R.id.txtNumeroCartao)
        val txtNome = includeFrente?.findViewById<TextView>(R.id.txtNomeNoCartao)
            ?: findViewById(R.id.txtNomeNoCartao)
        val txtValidade = includeFrente?.findViewById<TextView>(R.id.txtValidadeNoCartao)
            ?: findViewById(R.id.txtValidadeNoCartao)
        // CVV e demais na parte de trás
        val txtCvv = includeVerso?.findViewById<TextView>(R.id.txtCvvNoCartao) ?: findViewById(R.id.txtCvvNoCartao)
        val txtNumeroVerso = includeVerso?.findViewById<TextView>(R.id.txtNumeroNoCartao) ?: findViewById(R.id.txtNumeroNoCartao)
        val txtAgenciaVerso = includeVerso?.findViewById<TextView>(R.id.txtAgenciaNoCartao) ?: findViewById(R.id.txtAgenciaNoCartao)
        val txtContaVerso = includeVerso?.findViewById<TextView>(R.id.txtContaNoCartao) ?: findViewById(R.id.txtContaNoCartao)

        // ImageView da bandeira na frente do cartão e no verso
        val imgBandeiraFrente = includeFrente?.findViewById<ImageView>(R.id.imageView) ?: run { try { findViewById<ImageView>(R.id.imageView) } catch (e: Exception) { null } }
        val imgBandeiraVerso = includeVerso?.findViewById<ImageView>(R.id.imageView3) ?: run { try { findViewById<ImageView>(R.id.imageView3) } catch (e: Exception) { null } }

        // função que detecta a bandeira a partir dos dígitos iniciais
        fun detectarBandeira(rawDigits: String): Int {
            val digits = rawDigits.filter { it.isDigit() }
            if (digits.isEmpty()) return R.drawable.elo

            // Prefixos conhecidos da Elo (lista ampliada, aproximada) - verificados em ordem decrescente de comprimento
            val prefixesElo = listOf(
                // 6-digit IINs
                "506699","438935","457631","457632","451416","431274","401178","401179","504175","636368","636297",
                // 5/4-digit and other common prefixes
                "50669","5067","5066","5041","5090","5091","5092","509048","509049","509050","4576","4011","6505",
                // 4-digit and 3-digit
                "6277","6362","6516","6504","6550"
            )
            for (p in prefixesElo.sortedByDescending { it.length }) {
                if (digits.startsWith(p)) {
                    Log.d(TAG, "Bandeira detectada: ELO por prefixo $p")
                    return R.drawable.elo
                }
            }

            // Mastercard: 51-55 ou 2221-2720
            val first2 = digits.take(2).toIntOrNull()
            if (first2 != null && first2 in 51..55) {
                Log.d(TAG, "Bandeira detectada: MASTERCARD por range 51-55")
                return R.drawable.mastercard
            }
            val first4 = digits.take(4).toIntOrNull()
            if (first4 != null && first4 in 2221..2720) {
                Log.d(TAG, "Bandeira detectada: MASTERCARD por range 2221-2720")
                return R.drawable.mastercard
            }

            // Visa: começa com 4 (verificar por último para não mascarar Elo que também começa com 4)
            if (digits.startsWith("4")) {
                Log.d(TAG, "Bandeira detectada: VISA (prefixo 4)")
                return R.drawable.visa
            }

            // fallback padrão: usa Elo como padrão neutro
            Log.d(TAG, "Bandeira detectada: fallback (ELO)")
            return R.drawable.elo
        }

        Log.d(TAG, "views do cartão obtidos: numero(front)=${txtNumero != null}, numero(back)=${txtNumeroVerso != null}, agencia=${txtAgenciaVerso != null}, conta=${txtContaVerso != null}")

        // Inicializa textos do verso com placeholders
        txtNumeroVerso.text = "#### #### #### ####"
        txtAgenciaVerso.text = "Agencia: ####"
        txtContaVerso.text = "Conta: ######"

        // Inicializa visuais a partir dos campos atuais (se o usuário já digitou algo antes de abrir a activity)
        fun atualizarNumeroInicial() {
            val raw = edtNumero.text?.toString()?.replace("\\s".toRegex(), "") ?: ""
            val agrupado = raw.chunked(4).joinToString(" ")
            txtNumero.text = if (agrupado.isEmpty()) "0000 0000 0000 0000" else agrupado
            txtNumeroVerso.text = if (agrupado.isEmpty()) "#### #### #### ####" else agrupado
        }

        fun atualizarNomeInicial() {
            val nome = edtNome.text?.toString() ?: ""
            txtNome.text = if (nome.isBlank()) "NOME DO TITULAR" else nome.uppercase()
        }

        fun atualizarValidadeInicial() {
            val dig = edtValidade.text?.toString()?.replace("[^0-9]".toRegex(), "") ?: ""
            val formatado = when {
                dig.length >= 3 -> dig.substring(0,2) + "/" + dig.substring(2, Math.min(4, dig.length))
                dig.length >= 1 -> dig
                else -> ""
            }
            txtValidade.text = if (formatado.isBlank()) "MM/AA" else formatado
        }

        fun atualizarCvvInicial() {
            val cvv = edtCVV.text?.toString() ?: ""
            txtCvv.text = if (cvv.isBlank()) "***" else cvv
        }

        fun atualizarAgenciaContaInicial() {
            val ag = edtAgencia.text?.toString() ?: ""
            val co = edtConta.text?.toString() ?: ""
            txtAgenciaVerso.text = if (ag.isBlank()) "Agencia: ####" else "Agencia: $ag"
            txtContaVerso.text = if (co.isBlank()) "Conta: ######" else "Conta: $co"
        }

        // chama as funções de inicialização
        atualizarNumeroInicial()
        atualizarNomeInicial()
        atualizarValidadeInicial()
        atualizarCvvInicial()
        atualizarAgenciaContaInicial()
        // aplica a bandeira inicial com base no valor atual do número
        val rawInicial = edtNumero.text?.toString()?.replace("\\s".toRegex(), "") ?: ""
        val resInicial = detectarBandeira(rawInicial)
        try { imgBandeiraFrente?.setImageResource(resInicial) } catch (e: Exception) { Log.w(TAG, "Falha ao setar bandeira inicial (frente): ${e.message}") }
        try { imgBandeiraVerso?.setImageResource(resInicial) } catch (e: Exception) { Log.w(TAG, "Falha ao setar bandeira inicial (verso): ${e.message}") }

        // mantém a última bandeira para não repetir toasts
        var ultimaBandeiraRes = resInicial

        fun nomeBandeira(res: Int): String {
            return when (res) {
                R.drawable.visa -> "VISA"
                R.drawable.mastercard -> "MASTERCARD"
                R.drawable.elo -> "ELO"
                else -> "DESCONHECIDA"
            }
        }

        // Formata número do cartão com espaços a cada 4 dígitos
        edtNumero.addTextChangedListener(object : TextWatcher {
            var atualizando = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (atualizando) return
                atualizando = true
                val digitos = s?.toString()?.replace("\\s".toRegex(), "") ?: ""
                val agrupado = digitos.chunked(4).joinToString(" ")
                if (agrupado != s.toString()) {
                    edtNumero.setText(agrupado)
                    edtNumero.setSelection(agrupado.length)
                }
                txtNumero.text = if (agrupado.isEmpty()) "0000 0000 0000 0000" else agrupado
                // atualiza também o verso
                txtNumeroVerso.text = if (agrupado.isEmpty()) "#### #### #### ####" else agrupado
                // detecta a bandeira e atualiza as imagens (frente e verso)
                val bandeiraRes = detectarBandeira(digitos)
                try {
                    imgBandeiraFrente?.setImageResource(bandeiraRes)
                } catch (e: Exception) { Log.w(TAG, "Falha ao setar bandeira (frente): ${e.message}") }
                try {
                    imgBandeiraVerso?.setImageResource(bandeiraRes)
                } catch (e: Exception) { Log.w(TAG, "Falha ao setar bandeira (verso): ${e.message}") }
                // mostra toast quando a bandeira muda (útil para depuração)
                if (bandeiraRes != ultimaBandeiraRes) {
                    android.widget.Toast.makeText(this@MainActivity, "Bandeira detectada: ${nomeBandeira(bandeiraRes)}", android.widget.Toast.LENGTH_SHORT).show()
                    ultimaBandeiraRes = bandeiraRes
                }
                // feedback visual rápido para confirmar atualização do número
                android.widget.Toast.makeText(this@MainActivity, "Número atualizado: $agrupado", android.widget.Toast.LENGTH_SHORT).show()
                atualizando = false
            }
        })

        edtNome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                txtNome.text = if (s.isNullOrBlank()) "NOME DO TITULAR" else s.toString().uppercase()
            }
        })

        edtValidade.addTextChangedListener(object : TextWatcher {
            var atualizando = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (atualizando) return
                atualizando = true
                val digitos = s?.toString()?.replace("[^0-9]".toRegex(), "") ?: ""
                val formatado = when {
                    digitos.length >= 3 -> digitos.substring(0,2) + "/" + digitos.substring(2, Math.min(4, digitos.length))
                    digitos.length >= 1 -> digitos
                    else -> ""
                }
                if (formatado != s.toString()) {
                    edtValidade.setText(formatado)
                    edtValidade.setSelection(formatado.length)
                }
                txtValidade.text = if (formatado.isBlank()) "MM/AA" else formatado
                atualizando = false
            }
        })

        edtCVV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val valor = s?.toString() ?: ""
                txtCvv.text = if (valor.isBlank()) "***" else valor
            }
        })

        // Virar para o verso quando CVV for focado, voltar para frente caso contrário
        val listenerFoco = OnFocusChangeListener { v, temFoco ->
            if (temFoco) {
                when (v.id) {
                    R.id.editCVV -> if (!versoVisivel) realizarFlip()
                    R.id.editNumero, R.id.editNome, R.id.editValidade -> if (versoVisivel) realizarFlip()
                }
            }
        }

        edtCVV.onFocusChangeListener = listenerFoco
        edtNumero.onFocusChangeListener = listenerFoco
        edtNome.onFocusChangeListener = listenerFoco
        edtValidade.onFocusChangeListener = listenerFoco

        // Atualiza agência e conta no verso
        edtAgencia.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val valor = s?.toString() ?: ""
                val texto = if (valor.isBlank()) "Agencia: ####" else "Agencia: $valor"
                txtAgenciaVerso.text = texto
                Log.d(TAG, "Agencia atualizada: $texto")
                android.widget.Toast.makeText(this@MainActivity, "Agencia: $texto", android.widget.Toast.LENGTH_SHORT).show()
            }
        })

        edtConta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val valor = s?.toString() ?: ""
                val texto = if (valor.isBlank()) "Conta: ######" else "Conta: $valor"
                txtContaVerso.text = texto
                Log.d(TAG, "Conta atualizada: $texto")
                android.widget.Toast.makeText(this@MainActivity, "Conta: $texto", android.widget.Toast.LENGTH_SHORT).show()
            }
        })

    }
}