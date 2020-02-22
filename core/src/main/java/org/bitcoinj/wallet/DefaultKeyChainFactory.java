/*
 * Copyright 2014 devrandom
 * Copyright 2019 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.wallet;

import org.bitcoinj.crypto.*;
import org.bitcoinj.script.Script;

import com.google.common.collect.ImmutableList;

/**
 * Default factory for creating keychains while de-serializing.
 */
public class DefaultKeyChainFactory implements KeyChainFactory {
    @Override
    public DeterministicKeyChain makeKeyChain(Protos.Key key, Protos.Key firstSubKey, DeterministicSeed seed,
            KeyCrypter crypter, boolean isMarried, Script.ScriptType outputScriptType,
            ImmutableList<ChildNumber> accountPath) {
        DeterministicKeyChain chain;
        if (isMarried)
            chain = new MarriedKeyChain(seed, crypter, outputScriptType, accountPath);
        else
            chain = new DeterministicKeyChain(seed, crypter, outputScriptType, accountPath);
        return chain;
    }

    @Override
    public DeterministicKeyChain makeWatchingKeyChain(Protos.Key key, Protos.Key firstSubKey,
            DeterministicKey accountKey, boolean isFollowingKey, boolean isMarried, Script.ScriptType outputScriptType)
            throws UnreadableWalletException {
        DeterministicKeyChain chain;
        if (isMarried)
            chain = new MarriedKeyChain(accountKey, outputScriptType);
        else if (isFollowingKey)
            chain = DeterministicKeyChain.builder().watchAndFollow(accountKey).outputScriptType(outputScriptType).build();
        else
            chain = DeterministicKeyChain.builder().watch(accountKey).outputScriptType(outputScriptType).build();
        return chain;
    }

    @Override
    public DeterministicKeyChain makeSpendingKeyChain(Protos.Key key, Protos.Key firstSubKey,
            DeterministicKey accountKey, boolean isMarried, Script.ScriptType outputScriptType)
            throws UnreadableWalletException {
        DeterministicKeyChain chain;
        if (isMarried)
            chain = new MarriedKeyChain(accountKey, outputScriptType);
        else
            chain = DeterministicKeyChain.builder().spend(accountKey).outputScriptType(outputScriptType).build();
        return chain;
    }

    @Override
    public DeterministicKeyChain makeSpendingFriendKeyChain(Protos.Key key, Protos.Key firstSubKey, DeterministicSeed seed,
                                                            KeyCrypter crypter, boolean isMarried, ImmutableList<ChildNumber> accountPath) throws UnreadableWalletException
    {
        DeterministicKeyChain chain;
        if (isMarried)
            throw new UnsupportedOperationException("Married Friend Keychains are not allowed");
        else if(accountPath.get(0).equals(ChildNumber.NINE_HARDENED) && /* allow any coin type */
                accountPath.get(2).equals(ChildNumber.FIVE_HARDENED) && accountPath.get(3).equals(ChildNumber.ONE_HARDENED))
            chain = new FriendKeyChain(seed, crypter, accountPath);
        else return new DeterministicKeyChain(seed, crypter, Script.ScriptType.P2PKH, accountPath);
        return chain;
    }
}
