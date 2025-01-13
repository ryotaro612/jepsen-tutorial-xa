# jepsen-xa

A Clojure library designed to ... well, that part is up to you.

## Usage

### Development

Run the app in REPL.
```clojure
(def system
  (do
    (if (resolve 'system)
      (ig/halt! system))
    (ig/init 
      (load-config {:log/level {:app :debug :other :info}
                    :instrument true
                    :server {:port 3000
                             :join false}
                    :db {:db1 {:port 55432 :host "127.0.0.1"}
                         :db2 {:port 55433 :host "127.0.0.1"}}}))))
```
## License

Copyright © 2025 Ryotaro Nakamura

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
