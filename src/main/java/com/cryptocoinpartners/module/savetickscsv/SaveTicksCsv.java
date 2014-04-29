package com.cryptocoinpartners.module.savetickscsv;

import au.com.bytecode.opencsv.CSVWriter;
import com.cryptocoinpartners.module.*;
import com.cryptocoinpartners.schema.*;
import org.apache.commons.configuration.Configuration;
import org.joda.time.Instant;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;


public class SaveTicksCsv extends ModuleListenerBase
{

    public static String[] headers = new String[] { "listing", "market", "base", "quote", "time", "last", "vol", "bid", "bidvol", "ask", "askvol" };


    public void initModule( Esper esper, Configuration config )
    {
        final String filename = config.getString("savetickscsv.filename");
        if( filename == null )
            throw new Error("You must specify savetickscsv.filename");
        try {
            writer = new CSVWriter(new FileWriter(filename));
            writer.writeNext(headers);
            writer.flush();
        }
        catch( IOException e ) {
            log.error("Could not write file "+filename);
        }
    }


    @When("select * from Tick")
    public void saveTick( Tick t ) {
        final MarketListing listing = t.getMarketListing();
        final String market = listing.getMarket().getSymbol();
        final Fungible base = listing.getBase();
        final Fungible quote = listing.getQuote();
        final Instant time = t.getTime();
        final BigDecimal price = t.getLastPrice();
        final BigDecimal vol = t.getAmount();
        final String bid = t.getBestBid() == null ? "" : t.getBestBid().getPrice().toEngineeringString();
        final String bidVol = t.getBestBid() == null ? "" : t.getBestBid().getAmount().toEngineeringString();
        final String ask = t.getBestAsk() == null ? "" : t.getBestAsk().getPrice().toEngineeringString();
        final String askVol = t.getBestAsk() == null ? "" : t.getBestAsk().getAmount().toEngineeringString();
        if( price != null ) {
            writer.writeNext(new String[] {
                                     listing.toString(),
                                     market,
                                     base.getSymbol(),
                                     quote.getSymbol(),
                                     time.toString(),
                                     price.toEngineeringString(),
                                     vol.toEngineeringString(),
                                     bid,
                                     bidVol,
                                     ask,
                                     askVol
                             }
            );
            try {
                writer.flush();
            }
            catch( IOException e ) {
                log.warn(e.getMessage(), e);
            }
        }
    }


    public void destroyModule()
    {
        try {
            writer.close();
        }
        catch( IOException e ) {
            log.error(e.getMessage(), e);
        }
        super.destroyModule();
    }


    private CSVWriter writer;
}