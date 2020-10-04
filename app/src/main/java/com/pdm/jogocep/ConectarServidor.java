package com.pdm.jogocep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pdm.jogocep.model.Jogador;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConectarServidor extends AppCompatActivity {

    TextView tvStatus, textPointS, textTentS,textStatusJogo,tvNumPìngsPongs;
    ServerSocket welcomeSocket;
    DataOutputStream socketOutput;
    BufferedReader socketEntrada;
    DataInputStream fromClient;
    boolean continuarRodando = false;
    Button btLigarServer,btJoga;
    long ponts, tentativ;
    Jogador jogServidor;
    String cepserv,cidadeserv,logradouroserv;

    TextView ipt;
    TextView tv;
    TextView end;
    TextView cep2;
    TextView cepini;
    TextView cepfim;
    private Handler handler = new Handler();  //permite acesso da thred para UI onde tem o Handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conectar_servidor);

        tvStatus = findViewById(R.id.textStatus);
        btLigarServer = findViewById(R.id.btConectaServer);
        btJoga = findViewById(R.id.btJogar1);
        cep2 = (TextView)findViewById(R.id.tvCep2);
        ipt = (TextView)findViewById(R.id.textIP);
        tv = (TextView)findViewById(R.id.textCidade2);
        end = (TextView)findViewById(R.id.textEnd);
        cepini=(TextView)findViewById(R.id.edtCepInicio);
        cepfim=(TextView)findViewById(R.id.tvCepFim);
        textStatusJogo=(TextView)findViewById(R.id.textStatus1);
        textPointS=(TextView)findViewById(R.id.textPont1);
        textTentS=(TextView)findViewById(R.id.textTenta1);

        //Recuperar os dados enviados
        Bundle dados = getIntent().getExtras();
        cepserv = dados.getString("CEP");
        cidadeserv = dados.getString("localidade");
        logradouroserv = dados.getString("logradouro");
        Log.v("PDM", "CEP: " + cepserv + ", Cidade: "+ cidadeserv + ", Logradouro: " + logradouroserv);
        //Configurar valores recuperados
        tv.setText(cidadeserv);
        end.setText(logradouroserv);
        cep2.setText(cepserv);
        jogServidor = new Jogador();
      jogServidor.setCEPServer(cepserv);

        Log.v("PDM ","CEP Server"+jogServidor.getCEPServer());
        Log.v("PDM ","CEP Server"+ cepserv);

        btJoga.setEnabled(false);



    }

    public void ligarServidor(View v) {
        ConnectivityManager connManager;
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        Network[] networks = connManager.getAllNetworks();


        for (Network minhaRede : networks) {
            NetworkInfo netInfo = connManager.getNetworkInfo(minhaRede);
            if (netInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                NetworkCapabilities propDaRede = connManager.getNetworkCapabilities(minhaRede);

                if (propDaRede.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

                    String macAddress = wifiManager.getConnectionInfo().getMacAddress();
                    Log.v("PDM", "Wifi - MAC:" + macAddress);

                    int ip = wifiManager.getConnectionInfo().getIpAddress();
                    String ipAddress = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

                    Log.v("PDM", "Wifi - IP:" + ipAddress);
                    tvStatus.setText("Ativo");
                    ipt.setText(ipAddress);

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ligarServerCodigo();
                        }
                    });
                    t.start();
                }

            }

        }
    }


    public void mandarCep(View v) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socketOutput != null) {
                        socketOutput.writeUTF("cepserv");
                        socketOutput.flush();
                        //pings++;
                        atualizarStatus();
                    } else {
                        tvStatus.setText("Cliente Desconectado");
                        btLigarServer.setEnabled(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

    }

        public void desconectar (View view) {
            try {
                if (socketOutput != null) {
                    socketOutput.close();
                }
                //Habilitar o Botão de Ligar
                btLigarServer.post(new Runnable() {
                    @Override
                    public void run() {
                        btLigarServer.setEnabled(true);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void ligarServerCodigo () {
            //Desabilitar o Botão de Ligar
            btLigarServer.post(new Runnable() {
                @Override
                public void run() {
                    btLigarServer.setEnabled(false);
                    btJoga.setEnabled(true);
                }
            });
            String CEPCliente = "";
            String result = "";
            try {
                Log.v("PDM", "Ligando o Server");
                welcomeSocket = new ServerSocket(9090);
                Socket connectionSocket = welcomeSocket.accept();
                Log.v("PDM", "Nova conexão");
                //atualizarStatus();


                //Instanciando os canais de stream
                fromClient = new DataInputStream(connectionSocket.getInputStream());
                socketOutput = new DataOutputStream(connectionSocket.getOutputStream());
                continuarRodando = true;
                //result = fromClient.readUTF();
                //while (continuarRodando) {//<< loop desnecessário, mandaria o CEP várias vezes, só precisa que envie uma
                    socketOutput.writeUTF(cepserv);//<< estava enviando a String CEPServ e não a variável cepserv
                    socketOutput.flush();
                    Log.v("PDM", "Enviou CEP "+ cepserv);
                    //atualizarStatus(); //<< retirado por dar erro, não usar a menos que se mude o conteúdo dessa função para que ela tenha alguma utilidade
                    //<< As linhas acima foram deslocadas para o início do código porque a intenção é enviar o CEP assim que o
                    //<< botão for apertado, e o readUTF fará o código ficar em espera, impedindo a execução do que estiver
                    //<< abaixo dele

                    //result = fromClient.readUTF();//<< está em espera de uma mensagem duas vezes, congelaria o código aqui, melhor que seja dentro do if
                    //Log.v("PDM", "readUTF Result = " + result);//<< desnecessária visto a mensagem acima
                    //CEPCliente = result;//<< desnecessária visto a mensagem acima
                    if (CEPCliente.compareTo("") == 0) {//<< teste modificado para testar se o CEP do cliente é igual a vazio, já que ele foi iniciado como vazio
                        Log.v("PDM", "Antes de ler");
                        result = fromClient.readUTF();
                        Log.v("PDM", "readUTF Result = " + result);
                        if (result.compareTo("") != 0) {
                            CEPCliente = result;
                        }
                        if (CEPCliente.compareTo("") != 0) {//<< testa se o CEP do cliente é diferente de vazio
                            jogServidor.setCEPCliente(CEPCliente);//<< armazena o CEP do cliente na instância jogServidor
                            final String finaldocep = CEPCliente.substring(3);//<< variável CEPCliente não pode ser declarada como final, pois é mudada no meio da função; aqui ela foi armazenada em outra variável declarada como final para que seja usada na inner class abaixo
                            cepfim.post(new Runnable() {//<< só se pode mudar elementos da interface de uma thread diferente através do método post
                                @Override
                                public void run() {
                                    cepfim.setText(finaldocep);//<< variável finaldocep só pode ser acessada de uma inner class porque foi declarada como final
                                }
                            });
                        }
                    }


                    Log.v("PDM", "readUTF Result = " + result);

                    //result = fromClient.readUTF();
                    //if (result.compareTo("PING") == 0) {<< está comparando se a msg recebida é "PING", não tem serventia
                        //enviar Pong
                        //pongs++;

                    //}
                //}

                Log.v("PDM", result);
                //Enviando dados para o servidor
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void somarNumPontos () {
            ponts++;
            atualizarStatus();

        }

        public void atualizarStatus () {
            //Método que vai atualizar os pings e pongs, usando post para evitar problemas com as threads
            tvNumPìngsPongs.post(new Runnable() {
                @Override
                public void run() {
                   // tvNumPìngsPongs.setText("Enviados " + pings + " Pings e " + pongs + " Pongs");
                }
            });
        }



    }
