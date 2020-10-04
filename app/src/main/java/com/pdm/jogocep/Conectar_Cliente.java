package com.pdm.jogocep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.pdm.jogocep.model.Jogador;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Conectar_Cliente extends AppCompatActivity {

    TextView tvStatus, tvNumPìngsPongs;
    Socket clientSocket;
    DataOutputStream socketOutput;
    BufferedReader socketEntrada;
    DataInputStream socketInput;
    private TextInputEditText IPS;
    Button btconecServer, btJoga;
    long pings, pongs;
    TextView cepfim;
    TextView cepinicio;
    String cepCli, cidadeCli, logradouroCli;
    TextView ipt;
    TextView tv, end2, cidade2, cep2;
    Jogador jogCliente;

    //private Handler handler = new Handler();  //permite acesso da thred para UI onde tem o Handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conectar__cliente);

        tvStatus = findViewById(R.id.textStatus1);
        btconecServer = findViewById(R.id.btConectaServer);
        btJoga = findViewById(R.id.btJogar2);

        IPS = (TextInputEditText) findViewById(R.id.edtIPServer);

        cep2 = (TextView) findViewById(R.id.tvCep3);
        cidade2 = (TextView) findViewById(R.id.textCidade3);
        end2 = (TextView) findViewById(R.id.textEnd3);
      cepinicio = (TextView) findViewById(R.id.edtCepInicio2);
        cepfim = (TextView) findViewById(R.id.tvCepFim2);//<< estava dando erro por tentar acessar o TextView tvCepFim, que é da activity ConectarServidor

        //Criando máscara para o IP
       // SimpleMaskFormatter smf2 = new SimpleMaskFormatter("NNN.NNN.N.NN");
       // MaskTextWatcher mtw2 = new MaskTextWatcher(IPS, smf2);
       // IPS.addTextChangedListener(mtw2);

        //Recuperar os dados enviados
        Bundle dados = getIntent().getExtras();
        cepCli = dados.getString("CEP");
        cidadeCli = dados.getString("localidade");
        logradouroCli = dados.getString("logradouro");
        Log.v("PDM", "CEP: " + cepCli + ", Cidade: "+ cidadeCli + ", Logradouro: " + logradouroCli);

      // Configurar valores recuperados
       cidade2.setText(cidadeCli);
       end2.setText(logradouroCli);
       cep2.setText(cepCli);
        jogCliente = new Jogador();
        jogCliente.setCEPCliente(cepCli);

        Log.v("PDM " + "CEP Cliente", cepCli);

        btJoga.setEnabled(false);


    }

    public void atualizarStatus() {
        //Método que vai atualizar os pings e pongs, usando post para evitar problemas com as threads
        tvNumPìngsPongs.post(new Runnable() {
            @Override
            public void run() {
             tvNumPìngsPongs.setText("Enviados " + pings + " Pings e " + pongs + " Pongs");
            }
        });
    }

    /*public void onClickConectar(View v) {
        ConnectivityManager connManager;
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connManager.getAllNetworks();
        for (Network minhaRede : networks) {
            NetworkInfo netInfo = connManager.getNetworkInfo(minhaRede);
            if (netInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                NetworkCapabilities propDaRede = connManager.getNetworkCapabilities(minhaRede);
                if (propDaRede.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            conectarCodigo();
                        }
                    });
                    t.start();
                }
            }
        }
    }*/
           public void onClickConectar (View v){
            final String ip = IPS.getText().toString();
            tvStatus.setText("Conectando em "+ ip);
               //tvStatus.post(new Runnable() {
              //     @Override
               //    public void run() {
                //       tvStatus.setText("Conectando em " + ip );
               //    }
              // });
               //String CEPServidor = "";
               //String result = "";
               Thread t = new Thread(new Runnable() {
                   @Override
                   public void run() {
               try {
                   clientSocket = new Socket(ip, 9090);
                   Log.v("PDM " , "Conectado "+ ip);

                   tvStatus.post(new Runnable() {
                       @Override
                       public void run() {
                           tvStatus.setText("Conectado com " + ip );
                           btJoga.setEnabled(true);
                          // btconecServer.setEnabled(false);
                       }
                   });
                   jogCliente.setPorta(9090);
                   socketOutput =
                           new DataOutputStream(clientSocket.getOutputStream()); //Envia dados
                   socketInput =
                           new DataInputStream(clientSocket.getInputStream());// Recebe dados

                   //while (socketInput != null) {//<< loop desnecessário, mandaria o CEP várias vezes, só precisa que envie uma
                   socketOutput.writeUTF(cepCli);//<< antes estava passando a String "CEPCli" em vez da variável cepCli
                   socketOutput.flush();
                   Log.v("PDM", "Enviou CEP "+ cepCli);
                   //atualizarStatus(); //<< retirado por dar erro, não usar a menos que se mude o conteúdo dessa função para que ela tenha alguma utilidade
                   //As linhas acima foram deslocadas para o início do código porque a intenção é enviar o CEP assim que o
                   //botão for apertado, e o readUTF fará o código ficar em espera, impedindo a execução do que estiver
                   //abaixo dele
                       String CEPServidor = "";
                       //if (jogCliente.getCEPServer() == null) {
                        if (CEPServidor.compareTo("") == 0) {// << acrescentado: vai verificar o cep do servidor até que tenha sido recebido
                            //if (result.compareTo("CEPServ") == 0) {<< desnecessário, essa String não está mais sendo enviada e era inútil
                            Log.v("PDM", "Antes de ler");
                            String result = socketInput.readUTF();//<< deslocado pra dentro do if, assim não lerá de novo se tiver recebido CEP
                            Log.v("PDM", "Result " + result);//<< deslocado junto com a linha anterior
                            if (result.compareTo("") != 0) {//<< testa se a mensagem recebida do outro jogador é vazia
                                CEPServidor = result;
                                Log.v("PDM", "CEPServidor " + CEPServidor);
                            }
                            if (CEPServidor.compareTo("") != 0) {//<< teste deslocado para dentro para armazenar o CEP do servidor assim que lido - teste mudado para comparar se a String é vazia, pois a variável é inicializada como vazia, não sendo mais nula em nenhuma hipótese
                                jogCliente.setCEPServer(CEPServidor);
                                final String finaldocep = CEPServidor.substring(3);//<< variável CEPCliente não pode ser declarada como final, pois é mudada no meio da função; aqui ela foi armazenada em outra variável declarada como final para que seja usada na inner class abaixo
                                cepfim.post(new Runnable() {//<< só se pode mudar elementos da interface de uma thread diferente através do método post
                                   @Override
                                    public void run() {
                                        cepfim.setText(finaldocep);//<< variável finaldocep só pode ser acessada de uma inner class porque foi declarada como final
                                    }
                                });
                            }
                            //}
                        }



                       if (jogCliente.getCEPCliente() != null && jogCliente.getCEPServer() != null) {
                           Log.v("PDM", "Abrindo jogo");

                                    /*if (result.compareTo("PING") == 0) {
                                //enviar Pong
                                pongs++;
                                socketOutput.writeUTF("PONG");
                                socketOutput.flush();
                                atualizarStatus();
                            }*/
                       }
                   //}



                    } catch (Exception e) {

                        tvStatus.post(new Runnable() {
                            @Override
                            public void run() {
                                tvStatus.setText("Erro na conexão com " + ip );
                                btJoga.setEnabled(false);
                            }
                        });

                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }


        public void mandarPing (View v) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (socketOutput != null) {
                            socketOutput.writeUTF("PING");
                            socketOutput.flush();
                            pings++;
                            atualizarStatus();
                        } else {
                            tvStatus.setText("Cliente Desconectado");
                            btconecServer.setEnabled(true);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();

        }
            public void somarNumPongs () {
                pongs++;
                atualizarStatus();


            }

    }


