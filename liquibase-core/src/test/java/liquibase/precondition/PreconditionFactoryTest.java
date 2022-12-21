package liquibase.precondition;

import liquibase.precondition.core.AndPrecondition;
import liquibase.precondition.core.OrPrecondition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class PreconditionFactoryTest {

    @Before
    public void setup() {
        PreconditionFactory.reset();

    }

    @After
    public void after() {
        PreconditionFactory.reset();

    }

    @Test
    public void getInstance() {
        assertNotNull(PreconditionFactory.getInstance());

        assertSame(PreconditionFactory.getInstance(), PreconditionFactory.getInstance());
    }

    @Test
    public void register() {
        PreconditionFactory factory = PreconditionFactory.getInstance();

        PreconditionFactory.reset();

        int builtIn = factory.getPreconditions().size();

        factory.register(new MockPrecondition());

        assertEquals(builtIn + 1, factory.getPreconditions().size());
    }

    @Test
    public void unregister_instance() {
        PreconditionFactory factory = PreconditionFactory.getInstance();

        PreconditionFactory.reset();

        int builtIn = factory.getPreconditions().size();

        factory.register(new OrPrecondition());
        factory.register(new AndPrecondition());

        assertEquals(builtIn, factory.getPreconditions().size());

        factory.unregister("and");
        assertEquals(builtIn - 1, factory.getPreconditions().size());
    }

    @Test
    public void reset() {
        PreconditionFactory instance1 = PreconditionFactory.getInstance();
        PreconditionFactory.reset();
        assertNotSame(instance1, PreconditionFactory.getInstance());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void builtInGeneratorsAreFound() {
        Map<String, Class<? extends Precondition>> generators = PreconditionFactory.getInstance().getPreconditions();
        assertTrue(generators.size() > 5);
    }

    @Test
    public void createPreconditions() {
        Precondition precondtion = PreconditionFactory.getInstance().create("and");

        assertNotNull(precondtion);
        assertTrue(precondtion instanceof AndPrecondition);
    }


}