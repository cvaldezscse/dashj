package org.bitcoinj.evolution;

import org.bitcoinj.core.*;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;

public class SubTxRegister extends SpecialTxPayload {

    public final int CURRENT_VERSION = 1;

    String userName;
    byte [] pubKeyId;
    MasternodeSignature signature;

    public SubTxRegister(NetworkParameters params, Transaction tx) {
        super(params, tx);
    }

    public SubTxRegister(int version, String userName, ECKey key) {
        super(version);
        this.userName = userName;
        pubKeyId = key.getPubKeyHash();
        length = 2 + userName.length() + VarInt.sizeOf(userName.length()) + 20 + 1;
        sign(key);
    }

    public SubTxRegister(int version, String userName, byte [] keyId) {
        super(version);
        this.userName = userName;
        pubKeyId = keyId;
        signature = MasternodeSignature.createEmpty();
        length = 2 + userName.length() + VarInt.sizeOf(userName.length()) + 20 + 1;
    }

    @Override
    protected void parse() throws ProtocolException {
        super.parse();
        userName = readStr();
        pubKeyId = readBytes(20);
        signature = new MasternodeSignature(params, payload, cursor);
        cursor += signature.getMessageSize();
        length = cursor - offset;
    }

    @Override
    protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        super.bitcoinSerializeToStream(stream);
        Utils.stringToByteStream(userName, stream);
        stream.write(pubKeyId);
        signature.bitcoinSerialize(stream);
    }

    public Sha256Hash getSignHash() {
        try {
            UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(getMessageSize());
            super.bitcoinSerializeToStream(bos);
            Utils.stringToByteStream(userName, bos);
            bos.write(pubKeyId);
            bos.write(0);
            return Sha256Hash.wrap(Sha256Hash.hashTwice(bos.toByteArray()));
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    @Override
    public Sha256Hash getHash() {
        try {
            UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(getMessageSize());
            bitcoinSerializeToStream(bos);
            return Sha256Hash.wrapReversed(Sha256Hash.hashTwice(bos.toByteArray()));
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    public String getName() {
        return "subTxRegister";
    }

    public Transaction.Type getType() {
        return Transaction.Type.TRANSACTION_SUBTX_REGISTER;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.append("txType", getName());
        result.append("version", version);
        result.append("userName", userName);
        result.append("pubKeyId", Utils.HEX.encode(pubKeyId));
        return result;
    }

    void sign(ECKey key) {
        Sha256Hash hash = getSignHash();
        signature = HashSigner.signHash(hash, key);
        unCache();
    }

    @Override
    protected void unCache() {
        super.unCache();
        length = 2 + VarInt.sizeOf(userName.length()) + userName.length() + signature.getMessageSize();
    }

    public String getUserName() { return userName; }
    public byte [] getPubKeyId() { return pubKeyId; }
    public MasternodeSignature getSignature() { return signature; }

    @Override
    public int getCurrentVersion() { return CURRENT_VERSION; }
}
