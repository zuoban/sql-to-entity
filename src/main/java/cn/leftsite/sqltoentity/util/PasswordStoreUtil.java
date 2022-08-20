package cn.leftsite.sqltoentity.util;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class PasswordStoreUtil {
    private static CredentialAttributes createCredentialAttributes(String key) {
        String serviceName = CredentialAttributesKt.generateServiceName("cn.leftsite.sql-to-entity", key);
        System.out.println(serviceName);
        return new CredentialAttributes(serviceName);
    }

    public static @Nullable Credentials retrieveCredentials(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        CredentialAttributes credentialAttributes = createCredentialAttributes(username);
        return PasswordSafe.getInstance().get(credentialAttributes);
    }

    public static void storeCredentials(String username, String password) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(username);
        PasswordSafe.getInstance().set(credentialAttributes, new Credentials(username, password));
    }
}
