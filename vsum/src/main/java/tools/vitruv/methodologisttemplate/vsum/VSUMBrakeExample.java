package tools.vitruv.methodologisttemplate.vsum;

import tools.vitruv.framework.vsum.VirtualModelBuilder;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.emf.common.util.URI;
import autosar.*;
import mir.reactions.autoSARToSimulink.AutoSARToSimulinkChangePropagationSpecification;
import mir.reactions.simuLinkTOAutoSAR.SimuLinkTOAutoSARChangePropagationSpecification;
import simulink.Parameter;
import simulink.SimulinkModel;
import simulink.SubSystem;
import tools.vitruv.change.propagation.ChangePropagationMode;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.views.ViewTypeFactory;
import tools.vitruv.framework.vsum.VirtualModel;

/**
 * This class provides an example how to define and use a VSUM.
 */
public class VSUMBrakeExample {
  public static void main(String[] args) {
    Path storagePath = Path.of("vsumBrakeCaseStudy");
    VirtualModel vsum = createDefaultVirtualModel(storagePath);
    vsum.setChangePropagationMode(ChangePropagationMode.TRANSITIVE_CYCLIC); 
    registerRootObjects(vsum, storagePath);
    addBasicCaseStudyComponentsToView(vsum);
    
  }

  public static void addBasicCaseStudyComponentsToView(VirtualModel vsum) {
    // Create a view that contains the basic components of the case study

    addBrakeAsAutoSARCompositeComponent(vsum);
    addBrakeDiscAsAutoSARSWComponent(vsum);
    addBrakeCaliperAsAutoSARSWComponent(vsum);
    addBrakePadAsAutoSARSWComponent(vsum);
    addSimulinkParameterToBrakeSystem(vsum);

  }


  public static void addSimulinkParameterToBrakeSystem(VirtualModel vsum) {
    Parameter idParameter = simulink.SimuLinkFactory.eINSTANCE.createParameter();
    idParameter.setName("9023");
    Parameter instanceName = simulink.SimuLinkFactory.eINSTANCE.createParameter();
    instanceName.setName("autonomous_vehicle01");

    CommittableView view = getDefaultView(vsum,List.of(SimulinkModel.class)).withChangeRecordingTrait();
    modifyView(view, (CommittableView v) -> {

      SubSystem brakeSystem = v.getRootObjects(SimulinkModel.class).iterator().next().getContains().stream()
                                  .filter(SubSystem.class::isInstance).map(SubSystem.class::cast).findFirst().orElseThrow();
      brakeSystem.getParameters().add(idParameter);
      brakeSystem.getParameters().add(instanceName);
    });
  }

  public static void addBrakeAsAutoSARCompositeComponent(VirtualModel vsum) {
    CommittableView view = getDefaultView(vsum,List.of(AutoSARModel.class)).withChangeRecordingTrait();
    
    modifyView(view, (CommittableView v) -> {
      CompositeSwComponent brake = AutoSARFactory.eINSTANCE.createCompositeSwComponent();
      brake.setName("BrakeSystem1.0");
      v.getRootObjects(AutoSARModel.class).iterator().next().getSwcomponent().add(brake);
    });

  }  

  public static void addBrakeDiscAsAutoSARSWComponent(VirtualModel vsum) {
    AtomicSwComponent brakeDisc = AutoSARFactory.eINSTANCE.createAtomicSwComponent();
    brakeDisc.setName("BrakeDisc1.0");
    CommittableView view = getDefaultView(vsum,List.of(AutoSARModel.class)).withChangeRecordingTrait();

    modifyView(view, (CommittableView v) -> {
      v.getRootObjects(AutoSARModel.class).iterator().next().getSwcomponent().add(brakeDisc);
      CompositeSwComponent brake = (CompositeSwComponent) v.getRootObjects(AutoSARModel.class).iterator().next().getSwcomponent()
                          .stream().filter(CompositeSwComponent.class::isInstance).findFirst().orElseThrow();
      brake.getAtomicswcomponent().add(brakeDisc);
    });
  }

  public static void addBrakeCaliperAsAutoSARSWComponent(VirtualModel vsum) {
    AtomicSwComponent brakeCaliper = AutoSARFactory.eINSTANCE.createAtomicSwComponent();
    brakeCaliper.setName("BrakeCaliper1.0");
    CommittableView view = getDefaultView(vsum,List.of(AutoSARModel.class)).withChangeRecordingTrait();

    modifyView(view, (CommittableView v) -> {
      v.getRootObjects(AutoSARModel.class).iterator().next().getSwcomponent().add(brakeCaliper);
      CompositeSwComponent brake = (CompositeSwComponent) v.getRootObjects(AutoSARModel.class).iterator().next().getSwcomponent()
                          .stream().filter(CompositeSwComponent.class::isInstance).findFirst().orElseThrow();
      brake.getAtomicswcomponent().add(brakeCaliper);
    });
  }

  public static void addBrakePadAsAutoSARSWComponent(VirtualModel vsum) {
    AtomicSwComponent brakePad = AutoSARFactory.eINSTANCE.createAtomicSwComponent();
    brakePad.setName("BrakePad1.0");
    CommittableView view = getDefaultView(vsum,List.of(AutoSARModel.class)).withChangeRecordingTrait();

    modifyView(view, (CommittableView v) -> {
      v.getRootObjects(AutoSARModel.class).iterator().next().getSwcomponent().add(brakePad);
      CompositeSwComponent brake = (CompositeSwComponent) v.getRootObjects(AutoSARModel.class).iterator().next().getSwcomponent()
                          .stream().filter(CompositeSwComponent.class::isInstance).findFirst().orElseThrow();
      brake.getAtomicswcomponent().add(brakePad);
    });
  }


  private static VirtualModel createDefaultVirtualModel(Path storagePath) {
    return new VirtualModelBuilder()
        .withStorageFolder(storagePath)
        .withUserInteractorForResultProvider(new TestUserInteraction.ResultProvider(new TestUserInteraction()))
        .withChangePropagationSpecifications(new AutoSARToSimulinkChangePropagationSpecification(), new SimuLinkTOAutoSARChangePropagationSpecification())
        .buildAndInitialize();
  }


  public static void registerRootObjects(VirtualModel virtualModel, Path filePath) {
          CommittableView view = getDefaultView(virtualModel,
                  List.of(AutoSARModel.class))
                  .withChangeRecordingTrait();
          modifyView(view, (CommittableView v) -> {
              v.registerRoot(
                      AutoSARFactory.eINSTANCE.createAutoSARModel(),
                      URI.createFileURI(filePath.toString() + "/autosar.model"));
          });

  }


  public static View getDefaultView(VirtualModel vsum, Collection<Class<?>> rootTypes) {
        var selector = vsum.createSelector(ViewTypeFactory.createIdentityMappingViewType("default"));
        selector.getSelectableElements().stream()
                .filter(element -> rootTypes.stream().anyMatch(it -> it.isInstance(element)))
                .forEach(it -> selector.setSelected(it, true));
        return selector.createView();
    }

  private static void modifyView(CommittableView view, Consumer<CommittableView> modificationFunction) {
    modificationFunction.accept(view);
    view.commitChanges();
  }

}
