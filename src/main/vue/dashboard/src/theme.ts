import { definePreset } from '@primeuix/themes'
import Aura from '@primeuix/themes/aura'

// Modern preset on top of Aura: indigo/violet primary, softer radii, slate surfaces.
const SafPreset = definePreset(Aura, {
    primitive: {
        borderRadius: {
            none: '0',
            xs: '4px',
            sm: '8px',
            md: '10px',
            lg: '14px',
            xl: '20px'
        }
    },
    semantic: {
        primary: {
            50: '{indigo.50}',
            100: '{indigo.100}',
            200: '{indigo.200}',
            300: '{indigo.300}',
            400: '{indigo.400}',
            500: '{indigo.500}',
            600: '{indigo.600}',
            700: '{indigo.700}',
            800: '{indigo.800}',
            900: '{indigo.900}',
            950: '{indigo.950}'
        },
        focusRing: {
            width: '3px',
            style: 'solid',
            color: '{primary.200}',
            offset: '0'
        },
        colorScheme: {
            light: {
                surface: {
                    0: '#ffffff',
                    50: '{slate.50}',
                    100: '{slate.100}',
                    200: '{slate.200}',
                    300: '{slate.300}',
                    400: '{slate.400}',
                    500: '{slate.500}',
                    600: '{slate.600}',
                    700: '{slate.700}',
                    800: '{slate.800}',
                    900: '{slate.900}',
                    950: '{slate.950}'
                }
            },
            dark: {
                surface: {
                    0: '#ffffff',
                    50: '{zinc.50}',
                    100: '{zinc.100}',
                    200: '{zinc.200}',
                    300: '{zinc.300}',
                    400: '{zinc.400}',
                    500: '{zinc.500}',
                    600: '{zinc.600}',
                    700: '{zinc.700}',
                    800: '{zinc.800}',
                    900: '{zinc.900}',
                    950: '{zinc.950}'
                }
            }
        }
    },
    components: {
        button: {
            root: {
                paddingY: '0.65rem',
                paddingX: '1.1rem',
                label: { fontWeight: '600' }
            }
        },
        card: {
            root: {
                borderRadius: '{border.radius.xl}',
                shadow: 'none'
            }
        }
    }
})

export default SafPreset
