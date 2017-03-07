package com.stripe.android.model;

import android.support.annotation.NonNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import static com.stripe.android.testharness.CardInputTestActivity.VALID_VISA_NO_SPACES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link SourceParams}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class SourceParamsTest {

    private static Card FULL_FIELDS_VISA_CARD =
            new Card(VALID_VISA_NO_SPACES,
                    12,
                    2050,
                    "123",
                    "Captain Cardholder",
                    "1 ABC Street",
                    "Apt. 123",
                    "San Francisco",
                    "CA",
                    "94107",
                    "US",
                    "usd");

    @Test
    public void createBancontactParams_hasExpectedFields() {
        SourceParams params = SourceParams.createBancontactParams(
                1000L,
                "Stripe",
                "return/url/3000",
                "descriptor");

        assertEquals(Source.BANCONTACT, params.getType());
        assertEquals(Source.EURO, params.getCurrency());
        assertEquals(1000L, params.getAmount());
        assertNotNull(params.getOwner());
        assertEquals("Stripe", params.getOwner().get("name"));
        assertNotNull(params.getRedirect());
        assertEquals("return/url/3000", params.getRedirect().get("return_url"));

        Map<String, Object> apiMap =
                validateAndGetSingleElementApiParameterMap(params, "bancontact");
        assertEquals("descriptor", apiMap.get("statement_descriptor"));
    }

    @Test
    public void createBitcoinParams_hasExpectedFields() {
        SourceParams params = SourceParams.createBitcoinParams(10L, "usd", "abc@def.ghi");

        assertEquals(Source.BITCOIN, params.getType());
        assertEquals(Source.USD, params.getCurrency());
        assertEquals(10L, params.getAmount());
        assertNotNull(params.getOwner());
        assertEquals(1, params.getOwner().size());
        assertEquals("abc@def.ghi", params.getOwner().get("email"));
    }

    @Test
    public void createCardParams_hasBothExpectedMaps() {
        SourceParams params = SourceParams.createCardParams(FULL_FIELDS_VISA_CARD);

        Map<String, Object> apiMap =
                validateAndGetSingleElementApiParameterMap(params, "card");
        assertEquals(VALID_VISA_NO_SPACES, apiMap.get("number"));
        assertEquals(12, apiMap.get("exp_month"));
        assertEquals(2050, apiMap.get("exp_year"));
        assertEquals("123", apiMap.get("cvc"));

        assertNotNull(params.getOwner());
        assertEquals(1, params.getOwner().size());
        Map<String, Object> addressMap = getMapFromOwner(params, "address");
        assertEquals("1 ABC Street", addressMap.get("line1"));
        assertEquals("Apt. 123", addressMap.get("line2"));
        assertEquals("San Francisco", addressMap.get("city"));
        assertEquals("CA", addressMap.get("state"));
        assertEquals("94107", addressMap.get("postal_code"));
        assertEquals("US", addressMap.get("country"));
    }

    @Test
    public void createGiropayParams_hasExpectedFields() {
        SourceParams params = SourceParams.createGiropayParams(
                150L,
                "Stripe",
                "stripe://return",
                "stripe descriptor");

        assertEquals(Source.GIROPAY, params.getType());
        assertEquals(Source.EURO, params.getCurrency());
        assertEquals(150L, params.getAmount());
        assertNotNull(params.getOwner());
        assertEquals("Stripe", params.getOwner().get("name"));
        assertNotNull(params.getRedirect());
        assertEquals("stripe://return", params.getRedirect().get("return_url"));

        Map<String, Object> apiMap =
                validateAndGetSingleElementApiParameterMap(params, "giropay");
        assertEquals("stripe descriptor", apiMap.get("statement_descriptor"));
    }

    @Test
    public void createGiropayParams_withNullStatementDescriptor_hasExpectedFieldsButNoApiParams() {
        SourceParams params = SourceParams.createGiropayParams(
                150L,
                "Stripe",
                "stripe://return",
                null);

        assertEquals(Source.GIROPAY, params.getType());
        assertEquals(Source.EURO, params.getCurrency());
        assertEquals(150L, params.getAmount());
        assertNotNull(params.getOwner());
        assertEquals("Stripe", params.getOwner().get("name"));
        assertNotNull(params.getRedirect());
        assertEquals("stripe://return", params.getRedirect().get("return_url"));
        assertNull(params.getApiParameterMap());
    }

    @Test
    public void createIdealParams_hasExpectedFields() {
        SourceParams params = SourceParams.createIdealParams(
                900L,
                "Default Name",
                "stripe://anotherurl",
                "something you bought",
                "SVB");
        assertEquals(Source.IDEAL, params.getType());
        assertEquals(Source.EURO, params.getCurrency());
        assertEquals(900L, params.getAmount());
        assertNotNull(params.getRedirect());
        assertEquals("stripe://anotherurl", params.getRedirect().get("return_url"));
        Map<String, Object> apiMap =
                validateAndGetSingleElementApiParameterMap(params, "ideal");
        assertEquals("something you bought", apiMap.get("statement_descriptor"));
        assertEquals("SVB", apiMap.get("bank"));
    }

    @Test
    public void createIdealParams_whenMissingOneOptionalField_hasNoApiParams() {
        SourceParams params1 = SourceParams.createIdealParams(
                900L,
                "Default Name",
                "stripe://anotherurl",
                null,
                "SVB");

        SourceParams params2 = SourceParams.createIdealParams(
                900L,
                "Default Name",
                "stripe://anotherurl",
                "something you bought",
                null);

        assertEquals(Source.IDEAL, params1.getType());
        assertEquals(Source.EURO, params1.getCurrency());
        assertEquals(Source.IDEAL, params2.getType());
        assertEquals(Source.EURO, params2.getCurrency());
        assertNull(params1.getApiParameterMap());
        assertNull(params2.getApiParameterMap());
    }

    @Test
    public void createSepaDebitParams_hasExpectedFields() {
        SourceParams params = SourceParams.createSepaDebitParams(
                "Jai Testa",
                "ibaniban",
                "44 Fourth Street",
                "Test City",
                "90210",
                "EI");

        assertEquals(Source.SEPA_DEBIT, params.getType());
        assertNotNull(params.getOwner());
        assertEquals("Jai Testa", params.getOwner().get("name"));
        Map<String, Object> addressMap = getMapFromOwner(params, "address");
        assertEquals("44 Fourth Street", addressMap.get("line1"));
        assertEquals("Test City", addressMap.get("city"));
        assertEquals("90210", addressMap.get("postal_code"));
        assertEquals("EI", addressMap.get("country"));

        Map<String, Object> apiMap =
                validateAndGetSingleElementApiParameterMap(params, "sepa_debit");
        assertEquals("ibaniban", apiMap.get("iban"));
    }

    @Test
    public void createSofortParams_hasExpectedFields() {
        SourceParams params = SourceParams.createSofortParams(
                50000L,
                "example://return",
                "UK",
                "a thing you bought");

        assertEquals(Source.SOFORT, params.getType());
        assertEquals(Source.EURO, params.getCurrency());
        assertEquals(50000L, params.getAmount());
        assertNotNull(params.getRedirect());
        assertEquals("example://return", params.getRedirect().get("return_url"));
        Map<String, Object> apiMap =
                validateAndGetSingleElementApiParameterMap(params, "sofort");
        assertEquals("UK", apiMap.get("country"));
        assertEquals("a thing you bought", apiMap.get("statement_descriptor"));
    }

    @Test
    public void createThreeDSecureParams_hasExpectedFields() {
        SourceParams params = SourceParams.createThreeDSecureParams(
                99000L,
                "brl",
                "stripe://returnaddress",
                "card_id_123");

        assertEquals(Source.THREE_D_SECURE, params.getType());
        // Brazilian Real
        assertEquals("brl", params.getCurrency());
        assertEquals(99000L, params.getAmount());
        assertNotNull(params.getRedirect());
        assertEquals("stripe://returnaddress", params.getRedirect().get("return_url"));

        Map<String, Object> apiMap =
                validateAndGetSingleElementApiParameterMap(params, "three_d_secure");
        assertEquals(1, apiMap.size());
        assertEquals("card_id_123", apiMap.get("card"));
    }

    private Map<String, Object> validateAndGetSingleElementApiParameterMap(
            @NonNull SourceParams params,
            @NonNull String sourceName) {
        assertNotNull(params.getApiParameterMap());
        assertEquals(1, params.getApiParameterMap().size());
        assertNotNull(params.getApiParameterMap().get(sourceName));
        return params.getApiParameterMap().get(sourceName);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMapFromOwner(
            @NonNull SourceParams params,
            @NonNull String mapName) {
        assertNotNull(params.getOwner());
        assertTrue(params.getOwner() instanceof Map);
        return (Map<String, Object>) params.getOwner().get(mapName);
    }
}