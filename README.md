# Mobile Fastly Cache Purger

Fastly is our CDN.

The mobile Fastly cache purger listens for front update messages from the fronts tool and uses the Fastly API to purge the affected collection paths.

This purging enables the updates to the fronts tool to be visible immediately after change, as opposed to the 5 minutes it currenly takes for a change to show in apps (the fronts response from MAPI are cached in Fastly with 5 mins TTL).

# Architecture

The mobile Fastly cache purger is an AWS lambda, which sends a purge request to the Fastly API once it's triggered.
The trigger for the lambda is when a message lands onto the queue (`mobile-fastly-cache-purger-CODE/PROD-mobile-fastl-frontsPurgeSqs`), which is subscribed to the fronts tool SNS topic (`facia-PROD/CODE-FrontsUpdateSNSTopic`).


# Cache purging

Any time there is an update to the fronts tool, the lambda will receive the message sent from the fronts tool. The handler of the `Purger Lambda` will extract the path ID from the messge and use the path to look up IDs of all collections within that front using the Config.json file stored in an S3 bucket of the CMS Fronts account.

It will then send a purge request to the Fastly API using the surrogate keys of the extracted collection IDs.
