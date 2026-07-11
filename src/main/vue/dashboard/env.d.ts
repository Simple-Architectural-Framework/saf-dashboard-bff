/// <reference types="vite/client" />

import Card from 'primevue/card';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import Message from 'primevue/message';

declare module '@vue/runtime-core' {
  export interface GlobalComponents {
    Card: typeof Card;
    Button: typeof Button;
    Tag: typeof Tag;
    Message: typeof Message;
  }
}