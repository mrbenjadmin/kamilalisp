package palaiologos.kamilalisp.runtime.dataformat.digest;

import org.apache.commons.codec.digest.DigestUtils;

public class Sha3384Digest extends DigestFunction {
    @Override
    protected String name() {
        return "digest:sha3384";
    }

    @Override
    String digest(String s) {
        return DigestUtils.sha3_384Hex(s);
    }

    @Override
    String digest(byte[] data) {
        return DigestUtils.sha3_384Hex(data);
    }
}
