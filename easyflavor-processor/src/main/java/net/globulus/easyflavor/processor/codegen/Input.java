package net.globulus.easyflavor.processor.codegen;

import net.globulus.easyflavor.processor.FlavorableInterface;
import net.globulus.easyflavor.processor.util.ProcessorLog;
import net.globulus.mmap.MergeInput;

import java.util.ArrayList;
import java.util.List;

public class Input implements MergeInput<Input> {

    public final List<String> flavorables;
    public final List<FlavorableInterface> fis;

    public Input(List<String> flavorables, List<FlavorableInterface> fis) {
        ProcessorLog.warn(null, "MY FlAVOR " + flavorables.size());
        for (String t : flavorables) {
            ProcessorLog.warn(null, "MY FlAVOR " + t);
        }
        ProcessorLog.warn(null, "MY fis " + fis.size());
        this.flavorables = flavorables;
        this.fis = fis;
    }

    @Override
    public Input mergedUp(Input other) {
        ProcessorLog.warn(null, "AAAA " + other.flavorables.size());
        ProcessorLog.warn(null, "VVVV " + other.fis.size());
        List<String> flavorables = new ArrayList<>(other.flavorables);
        flavorables.addAll(this.flavorables);
        List<FlavorableInterface> fis = new ArrayList<>(other.fis);
        fis.addAll(this.fis);
        return new Input(flavorables, fis);
    }
}
