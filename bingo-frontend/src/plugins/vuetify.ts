import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import '@mdi/font/css/materialdesignicons.css'

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#1A237E',    // Royal Blue
          secondary: '#FFD600',  // Gold (Accent)
          success: '#2E7D32',    // Emerald Green
          background: '#FAFAFA', // Snow White
          surface: '#F5F5F5',    // Ice Grey
        }
      }
    }
  },
  icons: {
    defaultSet: 'mdi',
  }
})
