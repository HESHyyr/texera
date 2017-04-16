package edu.uci.ics.textdb.exp.common;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import edu.uci.ics.textdb.api.dataflow.IPredicate;
import edu.uci.ics.textdb.exp.keywordmatcher.KeywordPredicate;
import edu.uci.ics.textdb.exp.keywordmatcher.KeywordSourcePredicate;
import edu.uci.ics.textdb.exp.regexsplit.RegexSplitPredicate;
import edu.uci.ics.textdb.exp.sampler.SamplerPredicate;
import edu.uci.ics.textdb.exp.sentiment.NlpSentimentPredicate;
import edu.uci.ics.textdb.exp.sink.tuple.TupleSinkPredicate;
import edu.uci.ics.textdb.exp.source.scan.ScanSourcePredicate;
import edu.uci.ics.textdb.exp.source.file.FileSourcePredicate;


/**
 * PredicateBase is the base for all predicates which follow the 
 *   Predicate Bean pattern.
 * 
 * Every predicate needs to register itself in the JsonSubTypes annotation
 *   so that the Jackson Library can map each JSON string to the correct type
 * 
 * @author Zuozhi Wang
 *
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, // logical user-defined type names are used (rather than Java class names)
        include = JsonTypeInfo.As.PROPERTY, // make the type info as a property in the JSON representation
        property = "operator_type" // the name of the JSON property indicating the type
)
@JsonSubTypes({ 
        @Type(value = KeywordPredicate.class, name = "KeywordMatcher"), 
        @Type(value = KeywordSourcePredicate.class, name = "KeywordSource"), 
        
        @Type(value = NlpSentimentPredicate.class, name = "NlpSentiment"),
        @Type(value = RegexSplitPredicate.class, name = "RegexSplit"),
        @Type(value = SamplerPredicate.class, name = "Sampler"),
        
        @Type(value = ScanSourcePredicate.class, name = "ScanSource"),
        @Type(value = FileSourcePredicate.class, name = "FileSink"),        
        @Type(value = TupleSinkPredicate.class, name = "ViewResults"),
})
public abstract class PredicateBase implements IPredicate {
    
}
