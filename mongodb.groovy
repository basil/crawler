#!./lib/runner.groovy
// Generates server-side metadata for MongoDB auto-installation
import org.htmlunit.html.*;
import net.sf.json.*
import org.htmlunit.WebClient

def wc = new WebClient()
wc.setCssErrorHandler(new org.htmlunit.SilentCssErrorHandler());
wc.getOptions().setJavaScriptEnabled(false);
wc.getOptions().setThrowExceptionOnScriptError(false);
wc.getOptions().setThrowExceptionOnFailingStatusCode(false);

def json = [];
[osx: ['i386', 'x86_64'],
    linux: ['i686', 'x86_64'],
    win32: ['i386', 'x86_64'],
    sunos5: ['i86pc', 'x86_64']
].each { osname, archs -> archs.each { arch ->
    HtmlPage p = wc.getPage("http://dl.mongodb.org/dl/$osname/$arch")
    p.getByXPath("//a[@href]").reverse().collect { HtmlAnchor e ->
        def m = e.getHrefAttribute() =~ /^.*mongodb-$osname-$arch-(.*?)\.(tgz|zip)$/
        if (m) {
            String version = "${osname}-${arch}-${m[0][1]}"
            json << [id:version, name:version, url:m[0][0].replace('http://', 'https://')]
        }
    }
}}

lib.DataWriter.write("org.jenkinsci.plugins.mongodb.MongoDBInstaller",JSONObject.fromObject([list:json]));
