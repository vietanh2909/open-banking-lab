<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        Consent
    <#elseif section = "form">
        <div>
            <p><b>TPP Client:</b> ${clientId}</p>
            <p><b>Requested scopes:</b> ${scopes}</p>
            <p><b>Consent ID:</b> ${consentId}</p>
        </div>

        <form id="kc-consent-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <button class="btn btn-primary" name="decision" value="approve" type="submit">Approve</button>
            <button class="btn btn-secondary" name="decision" value="reject" type="submit">Reject</button>
        </form>

    </#if>
</@layout.registrationLayout>
