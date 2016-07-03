package com.tencao;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

@SideOnly(Side.CLIENT)
public class ProxyConnect extends GuiScreen {

    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
    private final GuiScreen previousGuiScreen;
    private boolean cancel;
    private NetworkManager networkManager;
    private int tries;

    public ProxyConnect(GuiScreen previousScreen)
    {
        this.previousGuiScreen = previousScreen;
        this.connect();
    }

    private void connect(){
        Thread proxyCheck = new ProxyCheck();

        for(int i = 0; i < 1; i++) {
            try {
                proxyCheck.start();
                proxyCheck.join();
            } catch (InterruptedException e) {
                PHLogger.logFatal("Proxy Thread Interupted");
            } catch (Exception e) {
                if (tries <= 3) {
                    ProxyConnect.this.connect();
                }
            }
        }


        ProxyCheck.ProxyList proxy = ProxyCheck.getFastest();

        PHLogger.logInfo("Connecting to " + proxy.name() + " proxy");
        (new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet())
        {
            private static final String __OBFID = "CL_00000686";
            public void run()
            {
                InetAddress inetaddress = null;

                try
                {
                    if (ProxyConnect.this.cancel)
                    {
                        return;
                    }
                    tries ++;
                    PHLogger.logInfo("Attempt " + tries);

                    inetaddress = InetAddress.getByName(proxy.getIP());
                    ProxyConnect.this.networkManager = NetworkManager.provideLanClient(inetaddress, proxy.getPort());
                    ProxyConnect.this.networkManager.setNetHandler(new NetHandlerLoginClient(ProxyConnect.this.networkManager, ProxyConnect.this.mc, ProxyConnect.this.previousGuiScreen));
                    ProxyConnect.this.networkManager.scheduleOutboundPacket(new C00Handshake(5, proxy.getIP(), proxy.getPort(), EnumConnectionState.LOGIN), new GenericFutureListener[0]);
                    ProxyConnect.this.networkManager.scheduleOutboundPacket(new C00PacketLoginStart(ProxyConnect.this.mc.getSession().getProfile()), new GenericFutureListener[0]);
                }
                catch (UnknownHostException unknownhostexception)
                {
                    if (ProxyConnect.this.cancel)
                    {
                        return;
                    }

                    PHLogger.logFatal("Connection Failed, please try again");
                    ProxyConnect.this.mc.displayGuiScreen(new GuiDisconnected(ProxyConnect.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {"Unknown host"})));
                }
                catch (NullPointerException nullexception){
                    if (tries <= 3) {
                        start();
                        return;
                    }


                    if (ProxyConnect.this.cancel)
                    {
                        return;
                    }

                    PHLogger.logFatal("Couldn\'t connect to server, please try again");
                    String s = nullexception.toString();

                    if (inetaddress != null)
                    {
                        String s1 = inetaddress.toString() + ":" + proxy.getPort();
                        s = s.replaceAll(s1, "");
                    }

                    ProxyConnect.this.mc.displayGuiScreen(new GuiDisconnected(ProxyConnect.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {s})));
                }
                catch (Exception exception)
                {
                    if (tries <= 3) {
                        start();
                        return;
                    }

                    if (ProxyConnect.this.cancel)
                    {
                        return;
                    }

                    PHLogger.logFatal("Couldn\'t connect to server" + exception);
                    String s = exception.toString();

                    if (inetaddress != null)
                    {
                        String s1 = inetaddress.toString() + ":" + proxy.getPort();
                        s = s.replaceAll(s1, "");
                    }

                    ProxyConnect.this.mc.displayGuiScreen(new GuiDisconnected(ProxyConnect.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {s})));
                }
            }
        }).start();
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        if (this.networkManager != null)
        {
            if (this.networkManager.isChannelOpen())
            {
                this.networkManager.processReceivedPackets();
            }
            else if (this.networkManager.getExitMessage() != null)
            {
                this.networkManager.getNetHandler().onDisconnect(this.networkManager.getExitMessage());
            }
        }
    }

    /**
     * Fired when a key is typed (except F11 who toggle full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) {}

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 2 + 50, I18n.format("gui.cancel", new Object[0])));
    }

    protected void actionPerformed(GuiButton button)
    {
        if (button.id == 0)
        {
            this.cancel = true;

            if (this.networkManager != null)
            {
                this.networkManager.closeChannel(new ChatComponentText("Aborted"));
            }

            this.mc.displayGuiScreen(this.previousGuiScreen);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        if (this.networkManager == null)
        {
            this.drawCenteredString(this.fontRendererObj, I18n.format("connect.connecting", new Object[0]), this.width / 2, this.height / 2 - 50, 16777215);
        }
        else
        {
            this.drawCenteredString(this.fontRendererObj, I18n.format("connect.authorizing", new Object[0]), this.width / 2, this.height / 2 - 50, 16777215);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
